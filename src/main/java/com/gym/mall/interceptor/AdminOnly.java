package com.gym.mall.interceptor;

import java.lang.annotation.*;

/**
 * 管理员权限注解
 *
 * 标注在 Controller 方法上，只有 ADMIN 角色的用户才能访问
 *
 * 知识点：自定义注解、AOP、反射、Spring 拦截器
 *
 * 使用示例：
 * @AdminOnly
 * @GetMapping("/admin/orders")
 * public Response<List<Order>> getOrders() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminOnly {
    String message() default "无权访问，仅管理员可操作";
}
