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

}
