package com.gym.mall.interceptor;

import com.gym.mall.utils.BaseContext;
import com.gym.mall.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * JWT 令牌拦截器
 * 负责在请求进入控制器之前校验 Token 的合法性，并提取用户信息
 */
@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 在请求处理之前进行拦截
     * 
     * @param request  HTTP 请求对象
     * @param response HTTP 响应对象
     * @param handler  被拦截的处理器（通常是 Controller 中的方法）
     * @return true 表示放行，false 表示拦截并阻止请求继续
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果拦截到的不是控制器方法（例如静态资源请求），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从 HTTP 请求头中获取名为 "token" 的数据
        String token = request.getHeader("token");

        // 2. 校验 Token 的合法性
        try {
            log.info("开始进行 JWT 校验, token: {}", token);
            
            // 解析 Token，如果 Token 过期或被篡改，这里会抛出异常
            Map<String, String> claims = jwtUtils.parseToken(token);
            
            // 从解析出的数据中获取用户 ID
            Long userId = Long.valueOf(claims.get("userId"));
            log.info("JWT 校验通过，当前登录用户 ID：{}", userId);
            
            // 3. 将解析出的用户 ID 存入 ThreadLocal (BaseContext)
            // 这样在后续的 Controller、Service 层中，可以通过 BaseContext.getCurrentId() 随时获取当前是谁在操作
            BaseContext.setCurrentId(userId);
            
            // 4. 校验通过，允许请求继续向下执行
            return true;
        } catch (Exception ex) {
            // 5. 校验失败（Token 错误、过期等）
            log.error("JWT 校验失败，原因: {}", ex.getMessage());
            
            // 设置 HTTP 状态码为 401（未授权），告诉前端需要重新登录
            response.setStatus(401);
            return false;
        }
    }

    /**
     * 在整个请求结束之后执行
     * 主要用于资源清理工作
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 关键步骤：请求结束后必须清理 ThreadLocal 中的数据
        // 理由：Tomcat 线程池会复用线程，如果不清理，下一个请求可能会读取到上一个请求的用户信息（导致身份错乱）
        BaseContext.removeCurrentId();
        log.info("请求结束，清理 ThreadLocal 中的用户信息");
    }
}
