package com.jy.nanding.demo_zk.zklock;

import com.jy.nanding.demo_zk.config.DefaultWatch;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

public class ZKUtils4Lock {

    private static ZooKeeper zk;

    private static String addr="192.168.8.129:2181,192.168.8.130:2181,192.168.8.131:2181,192.168.8.132:2181/testLock";

    private  static DefaultWatch watch = new DefaultWatch();

    private static CountDownLatch init =new CountDownLatch(1);

    public static ZooKeeper getZk(){
        try {//第一步创建zk连接，设置defaultWatch阻塞
            zk = new ZooKeeper(addr, 1000, watch);
            watch.setCdl(init);
            init.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  zk;
    }
}
