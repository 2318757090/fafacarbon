package com.animalcrossing.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {
    //自动启动内嵌tomcat服务器
    //自动创建Spring容器
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
