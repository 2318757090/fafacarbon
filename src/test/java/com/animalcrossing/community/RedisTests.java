package com.animalcrossing.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void testString(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }
    @Test
    public void testHash(){
        String rediskey = "test:user";
        redisTemplate.opsForHash().put(rediskey,"id",1);
        redisTemplate.opsForHash().put(rediskey,"name","zhangsan");
        redisTemplate.opsForHash().put(rediskey,"age","18");
        System.out.println(redisTemplate.opsForHash().get(rediskey,"id"));
        System.out.println(redisTemplate.opsForHash().get(rediskey,"age"));
    }
    @Test
    public void testList(){
        String rediskey = "test:ids";
        redisTemplate.opsForList().leftPush(rediskey,1);
        redisTemplate.opsForList().leftPush(rediskey,2);
        redisTemplate.opsForList().leftPush(rediskey,3);
        System.out.println(redisTemplate.opsForList().size(rediskey));
        System.out.println(redisTemplate.opsForList().index(rediskey,0L));
        System.out.println(redisTemplate.opsForList().range(rediskey,0,2));
        System.out.println(redisTemplate.opsForList().leftPop(rediskey));
        System.out.println(redisTemplate.opsForList().leftPop(rediskey));
        System.out.println(redisTemplate.opsForList().leftPop(rediskey));
    }
    @Test
    public void testSets(){
        String rediskey = "test:teacher";
        redisTemplate.opsForSet().add(rediskey,"zhangfei","guanyu","liubei");

        System.out.println(redisTemplate.opsForSet().size(rediskey));
        System.out.println(redisTemplate.opsForSet().pop(rediskey));
        System.out.println(redisTemplate.opsForSet().members(rediskey));
    }
    @Test
    public void testSortSets(){
        //有序集合
        String rediskey = "test:student";
        redisTemplate.opsForZSet().add(rediskey,"huahua",80);
        redisTemplate.opsForZSet().add(rediskey,"liangliang",90);
        redisTemplate.opsForZSet().add(rediskey,"fafa",100);
        redisTemplate.opsForZSet().add(rediskey,"qiuqiu",85);
        redisTemplate.opsForZSet().add(rediskey,"tiantian",70);

        System.out.println(redisTemplate.opsForZSet().zCard(rediskey));
        System.out.println(redisTemplate.opsForZSet().score(rediskey,"fafa"));
        System.out.println(redisTemplate.opsForZSet().rank(rediskey,"fafa"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(rediskey,"tiantian"));
        System.out.println(redisTemplate.opsForZSet().range(rediskey,0,2));
        System.out.println(redisTemplate.opsForZSet().reverseRange(rediskey,0,2));
    }
    @Test
    public void testKeys(){
        //删除一个key
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        //指定数据过期时间
        redisTemplate.expire("test:student",10, TimeUnit.SECONDS);
    }
    //多次访问同一个key
    @Test
    public void testBoundOperations(){
        String redisKey= "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }
    //编程式事务
    @Test
    public void testTransfer(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey= "test:tx";
                redisOperations.multi();
                redisOperations.opsForSet().add(redisKey,"zhangsan","zhaosi","wangwu");

                System.out.println(redisOperations.opsForSet().members(redisKey));
                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }

}
