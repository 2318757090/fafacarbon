package com.animalcrossing.community.controller.interceptor;

import com.animalcrossing.community.annotation.LoginRequired;
import com.animalcrossing.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            Method method = handlerMethod.getMethod();
            //获取方法上标注的注解
            if(method.getDeclaredAnnotation(LoginRequired.class)!=null&&hostHolder.getUser()==null){
                //重定向至登录页面
                response.sendRedirect(request.getContextPath()+"/login");
                //存在注解但用户并未登录
                return false;
            }
        }
        return true;
    }
}
