package com.animalcrossing.community.util;

import com.animalcrossing.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 * session的代替解决方案 防止多线程环境下变量的冲突
 */
@Component
public class HostHolder {
    public static final ThreadLocal<User> users = new ThreadLocal<>();

    public User getUser() {
        return users.get();
    }

    public void setUsers(User user) {
        users.set(user);
    }
    public void clear(){
        users.remove();
    }

}
