package com.animalcrossing.community.controller;

import com.animalcrossing.community.config.KaptchaConfig;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    private RedisTemplate redisTemplate;


    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path="/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path="/login",method = RequestMethod.GET)
    public String getLoginPage(Model model,KaptchaConfig kaptchaConfig){

        return "/site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if(map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));

            return "/site/register";
        }

    }
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId")int userId,@PathVariable("code")String code){
        int result = userService.activate(userId,code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，正在跳转到登录页面");
            model.addAttribute("target","/login");
        }else if(result==ACTIVATION_REPEAT){
            model.addAttribute("msg","请勿重复激活！");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，您的激活码错误！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response /*HttpSession session*/){
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //优化验证码 存入redis而不是session
        //session.setAttribute("kaptcha",text);
        String owner = CommunityUtil.generateUUID();
        //存入cookie
        Cookie cookie = new Cookie("kaptchaOwner",owner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //验证码存入redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(owner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);

        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("相应验证码失败"+e.getMessage());
        }
    }
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username, String password, String code,boolean rememberme,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchaOwner")String owner){
        //判断验证码合法性
        //String kaptcha = (String) session.getAttribute("kaptcha");
        //从cookie获取键 从redis中获取值
        String kaptcha = null;
        if(StringUtils.isNotBlank(owner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(owner);
            kaptcha = redisTemplate.opsForValue().get(kaptchaKey).toString();
        }

        if(StringUtils.isBlank(code)||StringUtils.isBlank(kaptcha)||!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("kaptchaMsg","验证码错误！");
            return "/site/login";
        }
        //进行登录操作
        int expiredSeconds = rememberme?REMEMBERME_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = new HashMap<>();
        map = userService.login(username,password,expiredSeconds);
        if(map.containsKey("ticket")){
            //代表登录成功 已经生成了通行证
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            //登录失败
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("kaptchaMsg",map.get("kaptchaMsg"));
            return "/site/login";

        }

    }
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
