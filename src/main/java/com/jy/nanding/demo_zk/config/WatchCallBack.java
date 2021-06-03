package com.jy.nanding.demo_zk.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    ZooKeeper zk;
    MyConfig mcf;
    CountDownLatch cdl = new CountDownLatch(1);

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public MyConfig getMcf() {
        return mcf;
    }

    public void setMcf(MyConfig mcf) {
        this.mcf = mcf;
    }

    public void aWait() {
        //异步方式获取
        zk.exists("/AppConf", this, this, "mark");
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                break;
            case NodeCreated:
                //第一次进来发现还没有节点，一直阻塞直到另一个线程创建了节点，然后把数据取出来
                zk.getData("/AppConf",this,this,"mark");
                break;
            case NodeDeleted:
                //数据一致性容忍性问题
                mcf.setConf("");
                cdl=new CountDownLatch(1);
                break;
            case NodeDataChanged:
                zk.getData("/AppConf",this,this,"mark");
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
    public void processResult(int i, String s, Object o, byte[] bytes, Stat stat) {
        if (null != bytes) {
            String data = new String(bytes);
            mcf.setConf(data);
            cdl.countDown();
        }

    }

    @Override
    public void processResult(int i, String s, Object o, Stat stat) {
        if (null != stat) {
            zk.getData("/AppConf", this, this, "mark");
        }
    }


}
