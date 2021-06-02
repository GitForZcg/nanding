package com.jy.nanding.demo_redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TestRedis {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    @Qualifier("serialObj")
    StringRedisTemplate stringRedisTemplate;


    /**
     *  将需要的对象包装成redis 的hash数据结构需要的objectMap类型
     */
    @Autowired
    ObjectMapper objectMapper;


    public void testRedis (){
        //高级模板
//        redisTemplate.opsForValue().set("k1","hello");
//        System.out.println("redisTemplate = " + redisTemplate.opsForValue().get("k1"));

//        stringRedisTemplate.opsForValue().set("k2","helloworld");
//        System.out.println("stringRedisTemplate = " + stringRedisTemplate.opsForValue().get("k2"));
        //低级模板
//        RedisConnection redisCon = redisTemplate.getConnectionFactory().getConnection();
//        redisCon.set("k3".getBytes(),"junyi".getBytes());
//        System.out.println(new String(redisCon.get("k3".getBytes())));


//        HashOperations<String, Object, Object> hash = stringRedisTemplate.opsForHash();
//        hash.put("myinfo","name","junyi");
//        hash.put("myinfo","age","25");
//        System.out.println(hash.entries("myinfo"));

        Person person = new Person();
        person.setName("junyi");
        person.setAge(25);

        //redisTemplate 方式
        //Jackson2HashMapper 将需要的对象包装成map类型的对象
//        Jackson2HashMapper jm = new Jackson2HashMapper(objectMapper, false);
//        HashOperations hash = redisTemplate.opsForHash();
//        hash.putAll("person",jm.toHash(person));
//        Map map = hash.entries("person");
//        System.out.println("hash.get() = "+ jm.fromHash(map));
//        Person p = objectMapper.convertValue(map, Person.class);
//        System.out.println(p);


//        //StringRedisTemplate 方式
//        //将需要放入的对象 使用Jackson2JsonRedisSerializer先序列化，缺点 每一个对象都要调用序列化器
//        stringRedisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
//        //Jackson2HashMapper 将需要的对象包装成map类型的对象
//        Jackson2HashMapper jm = new Jackson2HashMapper(objectMapper, false);
//        HashOperations<String, Object, Object> shash = stringRedisTemplate.opsForHash();
//        shash.putAll("person1",jm.toHash(person));
//        Map map = shash.entries("person1");
//        System.out.println("hash.get() = "+ jm.fromHash(map));
//        Person p = objectMapper.convertValue(map, Person.class);
//        System.out.println(p);

        //StringRedisTemplate 优化方式 ，将序列化抽出去
        //Jackson2HashMapper 将需要的对象包装成map类型的对象
        Jackson2HashMapper jm = new Jackson2HashMapper(objectMapper, false);
        HashOperations<String, Object, Object> shash = stringRedisTemplate.opsForHash();
        shash.putAll("person2",jm.toHash(person));
        Map map = shash.entries("person2");
        System.out.println("hash.get() = "+ jm.fromHash(map));
        Person p = objectMapper.convertValue(map, Person.class);
        System.out.println(p);

        //发布订阅模式
        //最简单的发布消息和订阅消息
        stringRedisTemplate.convertAndSend("note1","我是一条redis发的消息");

        RedisConnection conn = stringRedisTemplate.getConnectionFactory().getConnection();
        conn.subscribe(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] bytes) {
                byte[] body = message.getBody();
                System.out.println(new String(body));
            }
        }, "note1".getBytes());

        while(true){
            stringRedisTemplate.convertAndSend("note1","来自我自己的消息");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
