package com.animalcrossing.community.util;

public interface CommunityConstant {
    //激活成功
    int ACTIVATION_SUCCESS = 0;
    //重复激活
    int ACTIVATION_REPEAT = 1;
    //激活失败
    int ACTIVATION_FAILURE = 2;
    //默认登录状态保持时间
    int DEFAULT_EXPIRED_SECONDS = 3600*12;
    //记住我状态保持时间
    int REMEMBERME_EXPIRED_SECONDS = 3600*24*14;
    //实体类型 帖子
    int ENTITY_TYPE_POST = 1;
    //实体类型 回复
    int ENTITY_TYPE_COMMENT = 2;
    //实体类型 用户
    int ENTITY_TYPE_USER = 3;
    //消息主题类型 评论
    String TOPIC_COMMENT = "comment";
    //消息主题类型 点赞
    String TOPIC_LIKE = "like";
    //消息主题类型 关注
    String TOPIC_FOLLOW = "follow";
    //消息主题类型_发帖
    String TOPIC_PUBLISH = "publish";
    //消息主题类型_删除帖
    String TOPIC_DELETE = "delete";
    //系统ID (站内信)
    int SYSTEM_USER_ID = 1;
    //系统角色 普通用户
    String AUTHORITY_USER = "user";
    //系统角色 管理员
    String AUTHORITY_ADMIN = "admin";
    //系统角色 版主
    String AUTHORITY_MODERATOR = "moderator";



}
