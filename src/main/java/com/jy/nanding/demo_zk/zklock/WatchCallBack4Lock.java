package com.jy.nanding.demo_zk.zklock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallBack4Lock implements Watcher, AsyncCallback.StringCallback , AsyncCallback.Children2Callback , AsyncCallback.StatCallback {

    ZooKeeper zk;
    String threadName;
    CountDownLatch cdl = new CountDownLatch(1);

    String  pathName;

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }


    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public void tryLock(){
        try {
//            if(zk.getData("/")) 判断/下当前锁是否已经被使用过，没有使用走正常逻辑，有直接跳出
            zk.create("/lock",
                    threadName.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL,
                    this,
                    "lockMark");
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock(){
        try {
            zk.delete(pathName,-1);
            System.out.println(threadName + " release lock....");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                //删除节点事件 并不是广播的，因为每个节点只会看前面节点是否存在，所以只要这个节点消失，对外发送这一个事件
                //是不是所有线程都重新getChildren 判定自己节点的顺序？
                //只有被删除节点的后一个节点会重新getChildren 判定自己的顺序是不是第一个，只有后一个节点才能收到回调事件
                //假设 1 2 3 4  节点，如果不是第一个节点，假设当前3节点挂掉，也能造成4节点收到这个通知，从而让4节点 去watch 2 节点
                zk.getChildren("/",false,this,"lockMark");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
            case PersistentWatchRemoved:
                break;
        }
    }


    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if( null!= name ){
            pathName = name;
            System.out.println(threadName + " create node " + pathName);
            //此处是获取加锁的最小节点，【获取/节点下的子节点，所以不需要开启watch，开启watch，watch会监控/节点下所有的节点，每个节点只需监控前面的节点就可以了】
            //Thread-6 create node /lock0000000003
            //Thread-2 create node /lock0000000004
            //Thread-1 create node /lock0000000005
            //Thread-5 create node /lock0000000006
            //Thread-0 create node /lock0000000007
            //Thread-4 create node /lock0000000008
            //Thread-9 create node /lock0000000009
            //Thread-3 create node /lock0000000010
            //Thread-7 create node /lock0000000011
            //Thread-8 create node /lock0000000012
            //所有线程都对自己的节点创建完成，并且可以看到自己前面的节点
            zk.getChildren("/",false,this,"lockMark");
        }
    }

    //get  children callback
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        //一定能看到自己前面的节点
        // 每个线程拿到的都是乱序的，所以需要排序
        System.out.println(threadName+ " look locks......");
//        for (String child : children) {
//            System.out.println("child = " + child);
//        }
        Collections.sort(children);
        int i = children.indexOf(pathName.substring(1));
        //判断是不是第一个
        //是第一个就countDown
        if(i== 0){
            System.out.println(threadName+" i am first");

            try {
                //这一步是解决 第一个线程拿锁操作完成释放，导致后一个线程watch不到事件
                // 在countDown之前，谁获得锁了，谁就把锁的信息写到锁所在目录的node中去
                zk.setData("/",threadName.getBytes(),-1);
                cdl.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //不是第一个,就去获取最小的一个,监控前面的节点，如果前面的节点发生删除的事件，才会回调自己
            // 第一个this  表示 只关心前面一个节点的回调
            zk.exists("/"+children.get(i-1),this,this,"lockMark");
        }


    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
