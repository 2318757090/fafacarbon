package com.animalcrossing.community.service;

import com.animalcrossing.community.dao.CommentMapper;
import com.animalcrossing.community.entity.Comment;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType,int entityId,int offset,int limit){
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }
    public int findCommentsCount(int entityType,int entityId){
        return commentMapper.selectCommentsRows(entityType,entityId);
    }
    //添加评论的同时更新帖子的评论数 借助事务来完成
    @Transactional(isolation = Isolation.READ_COMMITTED ,propagation = Propagation.NESTED)
    public int addComment(Comment comment){
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //首先对内容进行过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //添加评论
        int rows = commentMapper.insertComment(comment);
        //更新帖子的评论数
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //是帖子的评论
            int commentCount = commentMapper.selectCommentsRows(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),commentCount);
        }
        return rows;
    }
    public  Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
}
