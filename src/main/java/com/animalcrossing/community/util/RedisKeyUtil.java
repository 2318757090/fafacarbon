package com.animalcrossing.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
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
    //登录凭证存储
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }
    //用户信息缓存
    public static String getUserKey(int userId){
        return PREFIX_USER+SPLIT+userId;
    }
    //单日UV
    public static String getUVKey(String date){
        return PREFIX_UV+SPLIT+date;
    }
    //区间UV
    public static String getUVKey(String fromDate,String toDate){
        return PREFIX_UV+SPLIT+fromDate+SPLIT+toDate;
    }
    //单日DAU
    public static String getDAUKey(String date){
        return PREFIX_DAU+SPLIT+date;
    }
    //区间DAU
    public static String getDAUKey(String fromDate,String toDate){
        return PREFIX_DAU+SPLIT+fromDate+SPLIT+toDate;
    }
}
