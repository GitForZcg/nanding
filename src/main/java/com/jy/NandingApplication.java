package com.jy;

import com.jy.nanding.demo_redis.TestRedis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class NandingApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(NandingApplication.class, args);
        TestRedis redis = context.getBean(TestRedis.class);
        redis.testRedis();
    }

}
