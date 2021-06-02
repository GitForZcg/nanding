package com.jy.nanding.demo_redis;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class MyRedisTemplate {

    @Bean
    public StringRedisTemplate serialObj(RedisConnectionFactory fc) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(fc);
        stringRedisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        return stringRedisTemplate;

    }

}
