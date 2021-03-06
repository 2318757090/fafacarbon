package com.animalcrossing.community;



import com.animalcrossing.community.dao.LoginTicketMapper;
import com.animalcrossing.community.dao.MessageMapper;
import com.animalcrossing.community.entity.LoginTicket;
import com.animalcrossing.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private MessageMapper messageMapper;

    /**
     * loginticket
     */
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("AABBCC");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void testSelectByTicket(){
        String ticket = "AABBCC";
        System.out.println(loginTicketMapper.selectByTicket(ticket));
    }
    @Test
    public void testUpdateStatus(){
        String ticket = "AABBCC";
        System.out.println(loginTicketMapper.updateStatus(ticket,1));
    }
    @Test
    public void testTime(){
        System.out.println(new Date(System.currentTimeMillis()+3600*24*15*1000));

        System.out.println(new Date(System.currentTimeMillis()));
    }
    /**
     * messageMapper
     */
    @Test
    public void testSelectConversations(){
        List<Message> messageList = messageMapper.selectConversations(111,0,Integer.MAX_VALUE);
        System.out.println(messageList);
    }
    @Test
    public void testSelectConversationRows(){
        System.out.println(messageMapper.selectConversationRows(111));

    }
    @Test
    public void testSelectConversationMessage(){
        List<Message> messages = messageMapper.selectConversationMessage("111_112",0,Integer.MAX_VALUE);
        System.out.println(messages);
    }
    @Test
    public void testSelectConversationMessageRows(){
        System.out.println( messageMapper.selectConversationMessageRows("111_112"));
    }
    @Test
    public void testSelectUnreadMessageCount(){
        System.out.println(messageMapper.selectUnreadMessageCount(111,null));
        System.out.println(messageMapper.selectUnreadMessageCount(111,"111_112"));
    }
}
