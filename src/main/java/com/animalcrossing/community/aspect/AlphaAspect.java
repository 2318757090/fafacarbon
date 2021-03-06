package com.animalcrossing.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    @Pointcut("execution(* com.animalcrossing.community.service.*.*(..))")
    public void pointcut(){
    }
    /**
     * 前后都织入
     * 前织入
     * 前面织入
     * 2021-03-01 13:26:04,142 DEBUG [http-nio-8080-exec-2] c.a.c.d.U.selectById [BaseJdbcLogger.java:143] ==>  Preparing: select id,username,password,salt,email,type,status,activation_code,header_url,create_time from user where id = ?
     * 2021-03-01 13:26:04,143 DEBUG [http-nio-8080-exec-2] c.a.c.d.U.selectById [BaseJdbcLogger.java:143] ==> Parameters: 138(Integer)
     * 2021-03-01 13:26:04,144 DEBUG [http-nio-8080-exec-2] c.a.c.d.U.selectById [BaseJdbcLogger.java:143] <==      Total: 1
     * 返回值后面织入
     * 后面织入
     * 后织入
     * 可以判断 靠近代码的顺序是 around-》before/after-》afterReturning
     */
    /**
     * 连接点开始
     */
    @Before("pointcut()")
    public void before(){
        System.out.println("前面织入");
    }
    @After("pointcut()")
    public void after(){
        System.out.println("后面织入");
    }
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("返回值后面织入");
    }
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("异常值后面织入");
    }

    /**
     * 在织入点前后写入逻辑 proceedingjointPoint就是织入点
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint ) throws Throwable{

        System.out.println("前后都织入");

        System.out.println("前织入");
        //调用目标组件的方法
        Object obj = proceedingJoinPoint.proceed();
        System.out.println("后织入");
        return obj;
    }
}
