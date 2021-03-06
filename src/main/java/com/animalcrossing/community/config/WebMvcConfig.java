package com.animalcrossing.community.config;

import com.animalcrossing.community.annotation.LoginRequired;
import com.animalcrossing.community.controller.interceptor.AlphaInterceptor;
import com.animalcrossing.community.controller.interceptor.LoginRequiredInterceptor;
import com.animalcrossing.community.controller.interceptor.LoginTicketInterceptor;
import com.animalcrossing.community.dao.LoginTicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AlphaInterceptor alphaInterceptor;
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.png","/**/*.jpeg")
                .addPathPatterns("/register","/login");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.png","/**/*.jpeg");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpg","/**/*.png","/**/*.jpeg");

    }
}
