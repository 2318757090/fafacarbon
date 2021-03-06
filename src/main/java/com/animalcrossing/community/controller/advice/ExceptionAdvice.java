package com.animalcrossing.community.controller.advice;

import com.animalcrossing.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * 处理controller中的异常
     * @param e 异常变量
     * @param response
     * @param request
     */
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request,HttpServletResponse response) throws IOException {
        //打印异常信息到日志
        logger.error("服务器发生异常"+e.getMessage());
        for(StackTraceElement element:e.getStackTrace()){
            logger.error(element.toString());
        }
        //异常信息打印完毕后 跳转到错误页面
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            //说明是异步请求 返回一个字符串
            response.setContentType("application/plain;character=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器发生异常"));
        }else{
            //html请求
            response.sendRedirect(request.getContextPath()+"/error");
        }

    }
}
