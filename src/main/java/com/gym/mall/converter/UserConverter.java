package com.gym.mall.converter;

import com.gym.mall.domain.entity.User;
import com.gym.mall.domain.dto.UserDTO;
import org.springframework.util.DigestUtils;

public class UserConverter {
    public static User convertToUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        // 在 Converter 中一次性完成密码加盐 + MD5 加密
        // 这样调用方只需要 save，不需要再手动 setPassword
        String salt = userDTO.getSalt();
        String md5Password = DigestUtils.md5DigestAsHex(
                (userDTO.getPassword() + salt).getBytes());
        return User.builder()
                .userId(userDTO.getId())
                .userName(userDTO.getUserName())
                .password(md5Password)
                .salt(salt)
                .role("USER")
                .build();
    }
}
