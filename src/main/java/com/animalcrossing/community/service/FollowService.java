package com.animalcrossing.community.service;

import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    /**
     * 当前用户关注实体操作
     */
    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //用户关注的
                String followerKey = RedisKeyUtil.getFollowerKey(userId, entityType);
                //被用户关注的
                String followeeKey = RedisKeyUtil.getFolloweeKey(entityType, entityId);
                redisOperations.multi();
                redisOperations.opsForZSet().add(followerKey,entityId,System.currentTimeMillis());
                redisOperations.opsForZSet().add(followeeKey,userId,System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }

    /**
     * 取关操作
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unFollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //用户关注的
                String followerKey = RedisKeyUtil.getFollowerKey(userId, entityType);
                //被用户关注的
                String followeeKey = RedisKeyUtil.getFolloweeKey(entityType, entityId);
                redisOperations.multi();
                redisOperations.opsForZSet().remove(followerKey,entityId);
                redisOperations.opsForZSet().remove(followeeKey,userId);
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询关注的实体数量
     * @param userId
     * @param entityType
     * @return
     */
    public Long findFollowerCount(int userId,int entityType){
        String followerKey = RedisKeyUtil.getFollowerKey(userId, entityType);
        Long count = redisTemplate.opsForZSet().zCard( followerKey);
        return count;
    }

    /**
     * 查询当前实体的粉丝数量
     * @param entityType
     * @param entityId
     * @return
     */
    public Long findFolloweeCount(int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(entityType, entityId);
        Long count = redisTemplate.opsForZSet().zCard(followeeKey);
        return count;
    }

    /**
     * 查询当前用户是否已关注实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followerKey,entityId)==null?false:true;
    }

    /**
     * 查询某个用户关注的人
     * @return
     */
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(userId,ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey,offset,offset+limit-1);
        if(targetIds==null) return null;
        //将ids转换为用户数据
        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer id:targetIds){
            //关注者信息
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user",user);
            //关注时间
            Double score = redisTemplate.opsForZSet().score(followerKey,id);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
    /**
     * 查询某个用户的粉丝
     * @return
     */
    public List<Map<String,Object>> findFollowees(int entityId,int offset,int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(ENTITY_TYPE_USER,entityId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey,offset,offset+limit-1);
        if(targetIds==null) return null;
        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer id:targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey,id);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }


}
