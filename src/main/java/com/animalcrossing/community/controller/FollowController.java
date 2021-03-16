package com.animalcrossing.community.controller;

import com.animalcrossing.community.entity.Event;
import com.animalcrossing.community.entity.Page;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.event.EventProducer;
import com.animalcrossing.community.service.FollowService;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;
    
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);
        //关注的时候没有帖子id参数
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注！");
    }
    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注！");
    }
    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String followList(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user==null) throw new RuntimeException("该用户不存在");
        model.addAttribute("user",user);
        page.setPath("/followers/"+userId);
        page.setLimit(10);
        page.setRows(followService.findFollowerCount(userId,ENTITY_TYPE_USER).intValue());
        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        //查看用户是否已被登录用户关注
        for (Map<String,Object> map:userList){
            User followUser = (User) map.get("user");
            map.put("isFollowed",hasFollowed(followUser.getId()));
        }
        model.addAttribute("followlist",userList);
        return "/site/followee";
    }
    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String fanList(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user==null) throw new RuntimeException("该用户不存在");
        model.addAttribute("user",user);
        page.setPath("/followees/"+userId);
        page.setLimit(10);
        page.setRows(followService.findFolloweeCount(ENTITY_TYPE_USER,userId).intValue());
        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        //查看用户是否已被登录用户关注
        for (Map<String,Object> map:userList){
            User followUser = (User) map.get("user");
            map.put("isFollowed",hasFollowed(followUser.getId()));
        }
        model.addAttribute("followlist",userList);
        return "/site/follower";
    }
    private boolean hasFollowed(int userId){
        if(hostHolder.getUser()==null) return false;
        boolean isFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        return isFollowed;
    }
}
