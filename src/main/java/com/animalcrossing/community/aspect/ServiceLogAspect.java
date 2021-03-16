package com.animalcrossing.community.aspect;

import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);
    @Pointcut("execution(* com.animalcrossing.community.service.*.*(..)))")
    public void pointcut(){ }
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //打印日志信息：用户[1.1.1.1] 在[时间] 访问了[方法路径]
        //获取用户IP
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //在使用消息队列发布站内信时 消费者获取消息的方式并不是通过request 所以汇报空指针异常错误 这里处理一下
        if(servletRequestAttributes==null){
            //表明是特殊调用
            //先简单处理 不记录日志
            return;
        }
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        String ip = httpServletRequest.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s]在[%s]时间访问了[%s]",ip,now,target));
    }
}
