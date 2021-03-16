package com.animalcrossing.community.service;

import com.animalcrossing.community.dao.LoginTicketMapper;
import com.animalcrossing.community.dao.UserMapper;
import com.animalcrossing.community.entity.LoginTicket;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.MailClient;
import com.animalcrossing.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        User user = getCache(id);
        if(user==null){
            user = initCache(id);
        }
        return user;
    }
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //空值 处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //验证账号 传入的username查询是否已存在
        User u = userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }
        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/用户id/激活码
        String url = domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }
    public int activate(int userId,String code){
        User user = userMapper.selectById(userId);

        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)) {
            clearCache(userId);
            userMapper.updateStatus(user.getId(),1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        //判断是否为空
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名称不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","用户名不存在");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","当前账户未激活");
            return map;
        }
        password=CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg", "密码错误");
            return map;
        }
        //处理ticket
        //登录 生成ticket存入redis
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);
//        LoginTicket loginTicket = loginTicketMapper.selectByuserId(user.getId());
//        if(loginTicket==null){
//            loginTicket = new LoginTicket();
//            loginTicket.setUserId(user.getId());
//            loginTicket.setStatus(0);
//            loginTicket.setTicket(CommunityUtil.generateUUID());
//            loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
//            loginTicketMapper.insertLoginTicket(loginTicket);
//        }else{
//            loginTicketMapper.updateStatus(loginTicket.getTicket(),0);
//            loginTicketMapper.updateExpired(loginTicket.getTicket(),new Date(System.currentTimeMillis()+expiredSeconds*1000));
//        }
        map.put("ticket",loginTicket.getTicket());
        return map;
    }
    public void logout(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }
    public LoginTicket findLoginTicketByTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        return loginTicket;
    }
    public int updateHeaderUrl(String headerUrl,int userId ){
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }
    public User findByUsername(String username){
        return userMapper.selectByName(username);
    }
    //因为用户的信息会被频繁调用 所以将用户信息存入缓存
    //1.先在缓存中查找用户信息
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(redisKey);
    }
    //2.找不到就初始化缓存
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(user.getId());
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //user信息发生变动时 清除缓存
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
    //根据用户id 获取权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = userMapper.selectById(userId);
        int type = user.getType();
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (type){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
