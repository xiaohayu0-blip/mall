package com.gym.mall.utils;

public class BaseContext {
    //BaseContext 是一个基于 ThreadLocal 的线程级用户信息持有者，
    // 让你在任何地方都能方便地获取当前登录用户，不用在方法参数里传来传去。

    public static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    public static ThreadLocal<String> roleLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }

    public static void removeCurrentId(){
        threadLocal.remove();
        roleLocal.remove();
    }

    public static void setCurrentRole(String role) {
        roleLocal.set(role);
    }

    public static String getCurrentRole() {
        return roleLocal.get();
    }
}
