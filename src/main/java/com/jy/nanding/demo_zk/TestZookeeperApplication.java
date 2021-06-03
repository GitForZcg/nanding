package com.jy.nanding.demo_zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class TestZookeeperApplication
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );

        //zk 使用session概念，没有连接池概念。每一个连接都有独立的session
        //watch 分为2类   第一类new zk时，传入的watch，这个watch session级别，跟path和node没关系
        //第一次启动   zk_state :: CONNECTING  使用CountDownLatch  做计数阻塞，直到zk启动完成
        //watch 注册只发生在读类型 ，比如调用get, exists, write属于写类型 产生事件
        final CountDownLatch cdl = new CountDownLatch(1);

        final ZooKeeper zk = new ZooKeeper("192.168.8.129:2181,192.168.8.130:2181,192.168.8.131:2181,192.168.8.132:2181",
                20000,
                new Watcher() {
                    //Watch的回调方法
                    @Override
                    public void process(WatchedEvent event) {
                        Event.KeeperState state = event.getState();
                        Event.EventType type = event.getType();
                        String path = event.getPath();
                        System.out.println("====zk event touch off====");
                        System.out.println("new zk watch" + event.toString());

                        switch (state) {
                            case Unknown:
                                System.out.println("event_type ::Unknown");
                                break;
                            case Disconnected:
                                System.out.println("event_type ::Disconnected");
                                break;
                            case NoSyncConnected:
                                System.out.println("event_type ::NoSyncConnected");
                                break;
                            //同步连接
                            case SyncConnected:
                                System.out.println("event_type ::SyncConnected");
                                System.out.println("connected");
                                cdl.countDown();
                                break;
                            case AuthFailed:
                                System.out.println("event_type ::AuthFailed");
                                break;
                            case ConnectedReadOnly:
                                System.out.println("event_type ::ConnectedReadOnly");
                                break;
                            case SaslAuthenticated:
                                System.out.println("event_type ::SaslAuthenticated");
                                break;
                            case Expired:
                                System.out.println("event_type ::Expired");
                                break;
                            case Closed:
                                System.out.println("event_type ::Closed");
                                break;
                        }

                        switch (type) {
                            case None:
                                System.out.println("event_type ::None");
                                break;
                            case NodeCreated:
                                System.out.println("event_type ::NodeCreated");
                                break;
                            case NodeDeleted:
                                System.out.println("event_type ::NodeDeleted");
                                break;
                            case NodeDataChanged:
                                System.out.println("event_type ::NodeDataChanged");
                                break;
                            case NodeChildrenChanged:
                                System.out.println("event_type ::NodeChildrenChanged");
                                break;
                            case DataWatchRemoved:
                                System.out.println("event_type ::DataWatchRemoved");
                                break;
                            case ChildWatchRemoved:
                                System.out.println("event_type ::ChildWatchRemoved");
                                break;
                            case PersistentWatchRemoved:
                                System.out.println("event_type ::PersistentWatchRemoved");
                                break;
                        }
                    }
                });
        cdl.await();
        ZooKeeper.States state = zk.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("zk_state :: CONNECTING");
                break;
            case ASSOCIATING:
                System.out.println("zk_state :: ASSOCIATING");
                break;
            case CONNECTED:
                System.out.println("zk_state :: CONNECTED");
                break;
            case CONNECTEDREADONLY:
                System.out.println("zk_state :: CONNECTEDREADONLY");
                break;
            case CLOSED:
                System.out.println("zk_state :: CLOSED");
                break;
            case AUTH_FAILED:
                System.out.println("zk_state :: AUTH_FAILED");
                break;
            case NOT_CONNECTED:
                System.out.println("zk_state :: NOT_CONNECTED");
                break;
        }

        String pathName = zk.create("/ooxx", "olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("created success pathName = " + pathName);
        final Stat stat = new Stat();
        //第一个取某个路径下的数据
        //第二个参数 Boolean watch, false代表非一次性监听;
        // true[default Watch 在new zk 的那个watch]代表该watch重新监听重新注册,可多次使用
        //最后一个参数为Stat类型 代表获取该节点的源数据
        byte[] node = zk.getData("/ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getDta event watch touch off::" + event.toString());
                try {
                    //true 使用默认的 default Watch ，在new zk 的那个watch
                    //new zk watchWatchedEvent state:SyncConnected type:NodeDataChanged path:/ooxx
//                    zk.getData("/ooxx",true,stat);
                    //使用内部类里面的watch
                    //getDta event watch touch off::WatchedEvent state:SyncConnected type:NodeDataChanged path:/ooxx
                    zk.getData("/ooxx",this,stat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, stat);
        System.out.println("node = " + new String(node));
        //最后一个参数 int version  设置版本号
        //触发回调
        Stat stat1 = zk.setData("/ooxx", "newData".getBytes(), 0);
        //下面是否会触发？
        Stat stat2= zk.setData("/ooxx", "newData01".getBytes(), stat1.getVersion());

//        //异步的方式
//        System.out.println(" ----------async start ----------" );
//        zk.getData("/ooxx", false, new AsyncCallback.DataCallback() {
//            @Override
//            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
//                System.out.println(" ----------async call back ----------" );
//                System.out.println("data= " + new String(data));
//                System.out.println("ctx= " + ctx.toString());
//            }
//        },"abc");
//
//        System.out.println("---------- async over----------");


        Thread.sleep(3000000);

    }
}
