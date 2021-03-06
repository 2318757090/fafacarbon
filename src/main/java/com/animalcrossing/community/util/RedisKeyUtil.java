package com.animalcrossing.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }
    public static String getUserLikeKey(int entityUserId){
        return PREFIX_USER_LIKE+SPLIT+entityUserId;
    }
    //某个用户关注的实体
    public static String getFollowerKey(int userId,int entityType){
        return PREFIX_FOLLOWER+SPLIT+userId+SPLIT+entityType;
    }
    //某个实体拥有的粉丝
    public static String getFolloweeKey(int entityType,int entityId){
        return PREFIX_FOLLOWEE+SPLIT+entityType+SPLIT+entityId;
    }
    //验证码存储
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA+SPLIT+owner;
    }
}
