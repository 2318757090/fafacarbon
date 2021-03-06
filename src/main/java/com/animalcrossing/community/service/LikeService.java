package com.animalcrossing.community.service;

import com.animalcrossing.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;
    //点赞操作
    public void like(int userId,int entityType,int entityId,int entityUserId){

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey,userId);
                //开启事务
                redisOperations.multi();
                if(isMember){
                    //已赞 取消赞
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else{
                    redisOperations.opsForSet().add(entityLikeKey,userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                //提交事务
                return redisOperations.exec();
            }
        });
    }
    //查询赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        Long count = redisTemplate.opsForSet().size(entityLikeKey);
        return count;
    }
    //查看某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        if(isMember){
            return 1;
        }else{
            return 0;
        }
    }
    //查看用户获得的赞总数
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count =  (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count.intValue();
    }
}
