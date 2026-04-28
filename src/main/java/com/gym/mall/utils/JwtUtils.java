package com.gym.mall.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    @Value("jwt.token.secretKey")
    private String secretKey;

    public String getToken(String userId,String userName){
        return null;
    }
}
