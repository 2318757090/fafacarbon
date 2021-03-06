package com.animalcrossing.community.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String getValue(HttpServletRequest request,String name){
        Cookie[] cookies = request.getCookies();
        if(request==null||cookies==null|| StringUtils.isBlank(name)){
            throw new IllegalArgumentException("参数为空!");
        }else{
            for(Cookie cookie:cookies){
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;

    }
}
