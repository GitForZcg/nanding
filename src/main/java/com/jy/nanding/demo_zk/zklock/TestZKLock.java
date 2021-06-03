package com.jy.nanding.demo_zk.zklock;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestZKLock {

    ZooKeeper zk;

    @Before
    public void conn() {
        zk = ZKUtils4Lock.getZk();
    }

    @After
    public void close() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lock() {

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WatchCallBack4Lock watch = new WatchCallBack4Lock();
                    watch.setZk(zk);
                    String threadName = Thread.currentThread().getName();
                    watch.setThreadName(threadName);
                    //每一个线程:
                    // 抢锁
                    watch.tryLock();
                    //干活
                    //干活过程太快 导致以下结果。thread0拿到锁 干完活释放掉后，其他线程没有进行后续操作
                    //thread0释放锁了，其他线程 Thread-5此时还是再watch监控前一个Thread-0 节点，Thread-5这个监控可能还没有完成 然后Thread-0把锁释放了。
                    //虽然Thread-5监控上了，但是由于thread-0 把锁释放了，Thread-5却看不到event事件了。
//                    Thread-0 create node /lock0000000040
//                    Thread-5 create node /lock0000000041
//                    Thread-1 create node /lock0000000042
//                    Thread-0 look locks...... 拿锁
//                    Thread-0 i am first    children最小的一个也是第一个
//                    Thread-0 start work......干活
//                    Thread-0 release lock....  释放锁
//                    System.out.println(threadName + " start work......");
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    System.out.println(threadName + " start work......");
                    //释放锁
                    watch.unLock();

                }
            }).start();

        }

        while (true){

        }
    }
}
