package com.animalcrossing.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.animalcrossing.community.dao.MessageMapper;
import com.animalcrossing.community.entity.Message;
import com.animalcrossing.community.entity.Page;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.service.MessageService;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @RequestMapping(path = "/letter/list" , method = RequestMethod.GET)
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
        model.addAttribute("letterUnreadCount",messageService.findUnReadMessageCount(user.getId(),null));
        //系统
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/letter";
    }
    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
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
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
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

    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        //获取系统中的通知消息
        //评论通知
        Message message = messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);
        if(message!=null){
            Map<String,Object> messageVo = new HashMap<>();
            //通知内容
            messageVo.put("message",message);
            //未读消息数量
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("count",count);
            messageVo.put("unreadCount",unreadCount);
            model.addAttribute("commentNotice",messageVo);
        }
        //点赞通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_LIKE);

        if(message!=null){
            Map<String,Object> messageVo = new HashMap<>();
            //通知内容
            messageVo.put("message",message);
            //未读消息数量
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("count",count);
            messageVo.put("unreadCount",unreadCount);
            model.addAttribute("likeNotice",messageVo);
        }

        //关注通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        if(message!=null){
            Map<String,Object> messageVo = new HashMap<>();
            //通知内容
            messageVo.put("message",message);
            //未读消息数量
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count",count);
            messageVo.put("unreadCount",unreadCount);
            model.addAttribute("followNotice",messageVo);
        }


        //查询未读消息数量
        //私信
        int letterUnreadCount = messageService.findUnReadMessageCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //系统
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,Model model,Page page){
        if(StringUtils.isBlank(topic)){
            throw new RuntimeException("通知参数错误");
        }
        User user = hostHolder.getUser();
        page.setRows(messageService.findNoticeCount(user.getId(),topic));
        page.setPath("/notice/detail/"+topic);
        page.setLimit(5);
        List<Message> noticeList = messageService.findNoticeByTopic(user.getId(),topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        for(Message notice:noticeList){
            Map<String,Object> noticeVo = new HashMap<>();
            //通知
            noticeVo.put("notice",notice);
            //内容
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            noticeVo.put("user",userService.findUserById((Integer) data.get("userId")));
            noticeVo.put("entityType",data.get("entityType"));
            noticeVo.put("entityId",data.get("entityId"));
            noticeVo.put("postId",data.get("postId"));
            //发送者
            noticeVo.put("fromUser",userService.findUserById(notice.getFromId()));
            noticeVoList.add(noticeVo);
        }
        model.addAttribute("noticeVoList",noticeVoList);
        //更新已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.updateStatus(ids);
        }

        return "/site/notice-detail";
    }

}
