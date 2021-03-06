package com.animalcrossing.community.dao;

import com.animalcrossing.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);
    //动态的拼接一个条件<if>里使用，并且方法有且只有一个参数，参数前必须取别名
    int selectDiscussPostRows(@Param("userId") int userId);
    int insertDiscussPost(DiscussPost discussPost);
    DiscussPost selectDiscussPostById(int id);
    int updateCommentCount(int id,int commentCount);
}
