package com.animalcrossing.community.controller;

import com.animalcrossing.community.entity.*;
import com.animalcrossing.community.event.EventProducer;
import com.animalcrossing.community.service.CommentService;
import com.animalcrossing.community.service.DiscussPostService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.HostHolder;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@RequestMapping(path = "/comment")
@Controller
public class CommentController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private CommentService commentService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        //评论成功后发送站内信
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(comment.getUserId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //评论帖子
            int targetId = discussPostService.findDiscussPostById(discussPostId).getUserId();
            event.setEntityUserId(targetId);
        }else if(comment.getEntityType()==ENTITY_TYPE_COMMENT){
            //评论评论
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //触发发帖事件 评论存到es服务器中
            //帖子的评论数量改变 需要修改es服务器中的帖子信息
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }

        return "redirect:/discuss/detail/"+discussPostId;

    }
}
