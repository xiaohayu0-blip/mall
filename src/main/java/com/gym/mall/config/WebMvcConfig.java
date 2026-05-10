package com.gym.mall.config;

import com.gym.mall.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 用于配置 Spring MVC 的相关行为，如拦截器、静态资源映射等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    /**
     * 注册自定义拦截器
     * 
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 JWT 令牌拦截器
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")             // 拦截所有路径（所有接口都需要登录）
                .excludePathPatterns("/user/login") // 排除登录接口（登录前拿不到 Token）
                .excludePathPatterns("/user");      // 排除注册接口（注册前也拿不到 Token）
    }
}
