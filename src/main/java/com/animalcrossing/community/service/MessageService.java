package com.animalcrossing.community.service;

import com.animalcrossing.community.dao.MessageMapper;
import com.animalcrossing.community.entity.Message;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;
import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }
    public int findConversationCount(int userId){
        return messageMapper.selectConversationRows(userId);
    }
    public List<Message> findConversationMessage(String conversationId,int offset,int limit) {
        return messageMapper.selectConversationMessage(conversationId, offset, limit);
    }
    public int findConversationMessageCount(String conversationId) {
        return messageMapper.selectConversationMessageRows(conversationId);
    }
    public int findUnReadMessageCount(int userId,String conversationId){
        return messageMapper.selectUnreadMessageCount(userId,conversationId);
    }
    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }
    public int updateStatus(List<Integer> ids){
        System.out.println(ids.size());
        return messageMapper.updateStatus(ids,1);
    }
}
