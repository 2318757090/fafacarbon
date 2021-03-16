package com.animalcrossing.community.dao;

import com.animalcrossing.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //获取当前用户的所有会话
    List<Message> selectConversations(int userId,int offset,int limit);
    //获取当前用户的会话数量
    int selectConversationRows(int userId);
    //获取会话中的信息
    List<Message> selectConversationMessage(String conversationId,int offset,int limit);
    //获取会话中的信息的数量
    int selectConversationMessageRows(String conversationId);
    //获取未读消息数量 1 单个会话 2 所有私信
    int selectUnreadMessageCount(int userId,String conversationId);
    //发送消息
    int insertMessage(Message message);
    //修改未读成为已读
    int updateStatus(List<Integer> ids,int status);
    //获取系统系统
    //查询某个主题下的最新通知
    Message selectLatestNotice(int userId,String topic);
    //查询某个主题下的通知数量
    int selectNoticeCount(int userId,String topic);
    //查询未读的通知数量
    int selectUnreadNoticeCount(int userId,String topic);
    //获取某个主题的通知详情
    List<Message> selectNoticeByTopic(int userId,String topic,int offset,int limit);

}
