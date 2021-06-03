package com.jy.nanding.demo_zk.config;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestConfig {

    ZooKeeper zk;

    @Before
    public void conn(){
        zk = ZKUtils.getZk();
    }

    @After
    public void close(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getConf(){
        WatchCallBack watchCallBack = new WatchCallBack();
        watchCallBack.setZk(zk);

        MyConfig myConfig = new MyConfig();
        watchCallBack.setMcf(myConfig);
        //异步方式获取
//        zk.exists("/AppConf", watchCallBack,watchCallBack,"mark");
        watchCallBack.aWait();

        while(true){
            if(myConfig.getConf().equals("")){
                System.out.println("conf diu le.......");
                watchCallBack.aWait();
            }else{
                System.out.println("myConfig = " + myConfig.getConf());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
