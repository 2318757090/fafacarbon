package com.animalcrossing.community.config;

import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Autowired
    private UserService userService;

    /**
     * 权限验证 授权
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception{
        //只有某些人可以访问的某些路径
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow")
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and().csrf().disable();
        //访问没有权限的异常时
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    //没登录时怎么处理
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //异步请求
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录！"));
                        }else{
                            //同步请求
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    //登录后权限不够如何处理
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            //异步请求
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限！"));
                        }else{
                            //同步请求
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/denied");
                        }
                    }
                });
        //security底层默认拦截logout请求 进行退出处理。覆盖这段默认逻辑 执行自己的退出逻辑
        http.logout().logoutUrl("/securitylogout");

    }

    /**
     * 忽略对静态资源的拦截
     * @param web
     * @throws Exception
     */

    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略静态资源的访问
        web.ignoring().antMatchers("/resources/**");
    }

    /**
     * 绕过系统的登录验证
     * @param auth
     * @throws Exception
     */
    //AuthenticationManager 认证核心接口
    //builder 用于构建上面对象的工具
    //ProviderManager 上接口的默认实现类
    //AuthenticationProvider ProviderManager持有一组ProviderManager 每一个AuthenticationProvider负责一种验证
    //委托模式：ProviderManager将认证委托给了AuthenticationProvider
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        //内置的认证规则
//        //auth.userDetailsService(userService).passwordEncoder(new Pbkdf2PasswordEncoder("12345"));
//        //自定义认证规则
//        auth.authenticationProvider(new AuthenticationProvider() {
//            // Authentication用于封装认证信息的接口 不同的实现类代表不同类型的认证信息
//            //认证逻辑
//            @Override
//            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//                String username = authentication.getName();
//                String password = (String)authentication.getCredentials();
//                User user = userService.findByUsername(username);
//                if(user==null){
//                    throw new UsernameNotFoundException("账号不存在");
//                }
//                password = CommunityUtil.md5(password+user.getSalt());
//                if(!user.getPassword().equals(password))
//                    throw new CredentialsExpiredException("密码错误");
//                //principal:认证的主要信息 user
//                //credentials:证书
//                //authorities：权限
//                return new UsernamePasswordAuthenticationToken(user,user.getPassword());
//            }
//            //支持的认证类型
//            @Override
//            public boolean supports(Class<?> aClass) {
//                //UsernamePasswordAuthenticationToken是Authentication的常用实现类
//                return UsernamePasswordAuthenticationToken.class.equals(aClass);
//            }
//        });
//    }
}
