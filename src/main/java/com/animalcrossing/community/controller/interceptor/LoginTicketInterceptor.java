package com.animalcrossing.community.controller.interceptor;

import com.animalcrossing.community.entity.LoginTicket;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CookieUtil;
import com.animalcrossing.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        //获取凭证信息
        LoginTicket loginTicket = userService.findLoginTicketByTicket(ticket);
        //检查凭证是否有效
        if(loginTicket!=null&&loginTicket.getStatus()==0&&loginTicket.getExpired().after(new Date())){
            //凭证有效
            //获取用户信息
            User user = userService.findUserById(loginTicket.getUserId());
            //在本次请求中保持用户信息
            hostHolder.setUsers(user);

        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user=hostHolder.getUser();

        if(modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
