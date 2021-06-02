package com.jy.nanding.demo_redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoRedisApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoRedisApplication.class, args);
        TestRedis redis = context.getBean(TestRedis.class);
        redis.testRedis();
    }

}
