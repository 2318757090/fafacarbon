package com.animalcrossing.community.controller;

import com.animalcrossing.community.dao.MessageMapper;
import com.animalcrossing.community.entity.Message;
import com.animalcrossing.community.entity.Page;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.service.MessageService;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.HostHolder;
import org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping(path = "/letter")
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @RequestMapping(path = "/list" , method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/letter/list");
        page.setLimit(10);

        List<Message> conversationList = messageService.findConversations(user.getId(),page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversationVoList = new ArrayList<>();
        for(Message conversation:conversationList){
            Map<String,Object> map = new HashMap<>();
            map.put("letter",conversation);
            map.put("letterCount",messageService.findConversationMessageCount(conversation.getConversationId()));
            map.put("unReadCount",messageService.findUnReadMessageCount(user.getId(),conversation.getConversationId()));
            int targetId = user.getId()==conversation.getFromId()?conversation.getToId():conversation.getFromId();
            map.put("target",userService.findUserById(targetId));
            conversationVoList.add(map);
        }
        model.addAttribute("conversations",conversationVoList);
        //总未读
        model.addAttribute("unReadCount",messageService.findUnReadMessageCount(user.getId(),null));
        return "/site/letter";
    }
    @RequestMapping(path = "/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page){
        //获取到会话中的所有消息
        User user = hostHolder.getUser();
        page.setLimit(10);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findConversationMessageCount(conversationId));
        List<Message> letterList = messageService.findConversationMessage(conversationId, page.getOffset(), page.getLimit());
        int targetId = letterList.get(0).getFromId()==user.getId()? letterList.get(0).getToId():letterList.get(0).getFromId();
        User target = userService.findUserById(targetId);
        //将未读消息状态置为已读
        List<Integer> idsUnread = this.getLetterIds(letterList);
        if(!idsUnread.isEmpty()){
            messageService.updateStatus(idsUnread);
        }
        model.addAttribute("target",target);
        model.addAttribute("letters",letterList);
        return "/site/letter-detail";
    }
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> list = new ArrayList<>();
        if(letterList==null) return null;
        for(Message letter:letterList){
            //发送给当前用户且未读的消息
            if(hostHolder.getUser().getId()==letter.getToId() && letter.getStatus() == 0){
                list.add(letter.getId());
            }

        }
        return list;
    }
    @RequestMapping(path = "/send",method = RequestMethod.POST)
    @ResponseBody
    public String addLetter(String recipientName,String messageText){
        User target = userService.findByUsername(recipientName);
        if(target==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在 ");
        }
        User user = hostHolder.getUser();
        Message message = new Message();
        message.setFromId(user.getId());
        message.setToId(target.getId());
        String conversationId = user.getId()>target.getId()?target.getId()+"_"+user.getId():user.getId()+"_"+target.getId();
        message.setConversationId(conversationId);
        message.setContent(messageText);
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0,"发送成功");
    }

}
