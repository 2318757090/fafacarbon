package com.animalcrossing.community.event;

import com.alibaba.fastjson.JSONObject;
import com.animalcrossing.community.entity.DiscussPost;
import com.animalcrossing.community.entity.Event;
import com.animalcrossing.community.entity.Message;
import com.animalcrossing.community.service.DiscussPostService;
import com.animalcrossing.community.service.ElasticSearchService;
import com.animalcrossing.community.service.MessageService;
import com.animalcrossing.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 事件消费者
 */
@Component
public class EventConsumer implements CommunityConstant {

    //
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleSystemMessage(ConsumerRecord record){
        //取出的消息 record.value()
        if(record==null){
            logger.error("消息的内容为空！");
            return;
        }
        Event event =  JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            logger.error("消息的格式错误！");
            return;
        }
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityId",event.getEntityId());
        content.put("entityType",event.getEntityType());
        if(!event.getData().isEmpty()){
            //消息体不为空
            for(Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        message.setStatus(0);
        messageService.addMessage(message);

    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handleESUpdateMessage(ConsumerRecord record){
        if(record==null){
            logger.error("消息的内容为空！");
            return;
        }
        Event event =  JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            logger.error("消息的格式错误！");
            return;
        }
        //首先查询出帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        //然后将帖子内容存储存储至es服务器
        elasticSearchService.saveDiscussPost(discussPost);

    }
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleESDeleteMessage(ConsumerRecord record){
        if(record==null){
            logger.error("消息的内容为空！");
            return;
        }
        Event event =  JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            logger.error("消息的格式错误！");
            return;
        }
        //从es服务器中删除帖子
        elasticSearchService.deleteDisscussPost(event.getEntityId());
    }
}
