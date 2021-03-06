package com.animalcrossing.community;

import com.animalcrossing.community.util.SensitiveFilter;
import org.junit.Test;


import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;



@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Test
    public void testFilter(){
        String result = sensitiveFilter.filter("&lt;html&gt;发票请找qq:12345678&lt;/html&gt;吸毒赌博哈哈哈哈哈啊哈、、、\\\\bukeyi hahahahhaha%*&&%$%^*$");
        System.out.println(result);
    }
}
