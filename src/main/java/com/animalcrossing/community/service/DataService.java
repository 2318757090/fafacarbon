package com.animalcrossing.community.service;

import com.animalcrossing.community.util.RedisKeyUtil;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyMMdd");
    //将指定IP计入UV
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }
    //统计指定范围日期内的UV
    public long calculateUV(Date fromDate,Date toDate){
        if(fromDate==null||toDate==null){
            throw new RuntimeException("参数不能为空");
        }
        //获取单日key
        List<String> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        while (!calendar.getTime().after(toDate)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            list.add(key);
            calendar.add(Calendar.DATE,1);
        }
        //存入聚合key
        String redisKey = RedisKeyUtil.getUVKey(df.format(fromDate),df.format(toDate));
        redisTemplate.opsForHyperLogLog().union(redisKey,list.toArray());
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }
    //将活跃用户计入DAU
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }
    //统计指定范围日期内的DAU
    public long calculateDAU(Date fromDate,Date toDate){
        if(fromDate==null||toDate==null){
            throw new RuntimeException("参数不能为空");
        }
        //获取单日key
        List<byte[]> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        while (!calendar.getTime().after(toDate)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            list.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        //进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(fromDate),df.format(toDate));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),list.toArray(new byte[0][0]));
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
    }
}
