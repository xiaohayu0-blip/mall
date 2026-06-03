package com.gym.mall.converter;

import com.gym.mall.domain.entity.User;
import com.gym.mall.domain.dto.UserDTO;

public class UserConverter {
    public static User converToUser(UserDTO userDTO) {
        if(userDTO==null){
            return null;
        }
        return User.builder()
                .user_id(userDTO.getId())
                .userName(userDTO.getUserName())
                .salt(userDTO.getSalt())
                .role("USER")
                .build();
    }
}
