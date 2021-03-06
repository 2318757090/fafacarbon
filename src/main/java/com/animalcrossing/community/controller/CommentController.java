package com.animalcrossing.community.controller;

import com.animalcrossing.community.entity.Comment;
import com.animalcrossing.community.entity.DiscussPost;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.service.CommentService;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@RequestMapping(path = "/comment")
@Controller
public class CommentController {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        return "redirect:/discuss/detail/"+discussPostId;

    }
}
