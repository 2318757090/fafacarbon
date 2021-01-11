package com.animalcrossing.community.dao;

import com.animalcrossing.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectById(int id);
    User selectByName(String username);
    User selectByEmail(String email);
    int insertUser(User user);
    int updatePassword(User user);
    int updateStatus(int id,int status);
    int updateHeader(int id,int headerUrl);
    int updatePassword(int id,int password);
}
