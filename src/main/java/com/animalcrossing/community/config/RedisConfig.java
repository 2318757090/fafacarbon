package com.animalcrossing.community.config;

import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        //注入连接工厂 访问数据库
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //主要配置序列化（数据转换）的方式
        //设置key的序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        //设置普通value的序列化方式
        redisTemplate.setValueSerializer(RedisSerializer.json());
        //设置hash的key序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        //触发设置另其生效
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
