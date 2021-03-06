package com.animalcrossing.community.dao;

import com.animalcrossing.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);
    int selectCommentsRows(int entityType,int entityId);
    int insertComment(Comment comment);
}
