package com.animalcrossing.community.controller;

import com.animalcrossing.community.dao.CommentMapper;
import com.animalcrossing.community.entity.Comment;
import com.animalcrossing.community.entity.DiscussPost;
import com.animalcrossing.community.entity.Page;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.service.CommentService;
import com.animalcrossing.community.service.DiscussPostService;
import com.animalcrossing.community.service.LikeService;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;


    @RequestMapping(path = "/add" , method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user  = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"您还没有登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        return CommunityUtil.getJSONString(0,"发布成功");
    }
    @RequestMapping(path = "/detail/{discussPostId}" , method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId")int discussPostId, Model model,Page page){
        //查询到帖子的详细内容
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        //查询发布者的详细信息（redis）
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        //获取帖子的点赞信息
        Long postLikeCount =  likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",postLikeCount);
        int postLikeStatus = hostHolder.getUser()==null?0:
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",postLikeStatus);
        //查询评论
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(discussPost.getCommentCount());
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST,discussPost.getId(), page.getOffset(), page.getLimit());

        //优化显示评论中的信息
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        for(Comment comment:commentList){
            Map<String,Object> commentVo = new HashMap<>();
            //每个评论中的信息
            commentVo.put("comment",comment);
            //评论的点赞数量
            Long commentLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
            commentVo.put("likeCount",commentLikeCount);
            //评论的点赞状态
            int commentLikeStatus = hostHolder.getUser()==null?0:
                    likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
            commentVo.put("likeStatus",commentLikeStatus);
            //评论的发出者
            commentVo.put("user",userService.findUserById(comment.getUserId()));
            //评论的回复列表
            List<Comment> replyList = commentService.findCommentsByEntity(
                    ENTITY_TYPE_COMMENT, comment.getId(), 0,Integer.MAX_VALUE);
            List<Map<String,Object>> replyVoList = new ArrayList<>();
            for(Comment reply:replyList){
                Map<String,Object> replyVo = new HashMap<>();
                //回复内容
                replyVo.put("comment",reply);
                //回复发出者
                replyVo.put("user",userService.findUserById(reply.getUserId()));
                //回复赞数
                Long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,reply.getId());
                replyVo.put("likeCount",replyLikeCount);
                //当前用户点赞状态
                int replyLikeStatus = hostHolder.getUser()==null?0:
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                replyVo.put("likeStatus",replyLikeStatus);
                //回复目标
                User target = reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                replyVo.put("target",target);
                replyVoList.add(replyVo);
            }
            commentVo.put("replys",replyVoList);
            commentVo.put("replyCount",commentService.findCommentsCount(ENTITY_TYPE_COMMENT,comment .getId()));
            commentVoList.add(commentVo);
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

}
