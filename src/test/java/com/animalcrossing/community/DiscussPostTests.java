package com.animalcrossing.community;

import com.animalcrossing.community.entity.DiscussPost;
import com.animalcrossing.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class DiscussPostTests {
    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void testInsert(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle("这是一个测试用例2");
        discussPost.setContent("这是一个测试用例2");
        discussPost.setCreateTime(new Date());
        discussPost.setUserId(155);
        discussPost.setStatus(0);

        discussPostService.addDiscussPost(discussPost);
        System.out.println(discussPost.getId());
    }
}
