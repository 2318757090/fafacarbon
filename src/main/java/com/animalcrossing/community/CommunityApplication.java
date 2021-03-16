package com.animalcrossing.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
    @PostConstruct
    public void init(){
        //解决netty启动冲突问题
        System.setProperty("es.set.netty.runtime.avaliable.processors","false");
    }
    //自动启动内嵌tomcat服务器
    //自动创建Spring容器
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
