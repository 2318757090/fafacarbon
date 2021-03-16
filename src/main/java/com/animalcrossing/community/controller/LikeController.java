package com.animalcrossing.community.controller;

import com.animalcrossing.community.entity.Comment;
import com.animalcrossing.community.entity.Event;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.event.EventProducer;
import com.animalcrossing.community.service.LikeService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int discussPostId){
        User user = hostHolder.getUser();
        //用户点赞操作
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //查询点赞数量
        long count = likeService.findEntityLikeCount(entityType,entityId);
        //查询点赞状态
        int status = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",count);
        map.put("likeStatus",status);
        //评论成功后发送站内信
        if(status==1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setData("postId",discussPostId)
                    .setEntityUserId(entityUserId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }
}
