package com.gym.mall.utils;

public class BaseContext {

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
