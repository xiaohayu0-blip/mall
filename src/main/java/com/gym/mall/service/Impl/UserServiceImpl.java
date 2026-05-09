package com.gym.mall.service.Impl;

import com.gym.mall.Repository.UserRepository;
import com.gym.mall.converter.UserConverter;
import com.gym.mall.dao.User;
import com.gym.mall.dto.UserDTO;
import com.gym.mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public long registerUser(UserDTO userDTO) {
        User user= UserConverter.converToUser(userDTO);
        // 1. 调用 Converter：把 DTO 里的 id、userName、salt 搬到 User 实体中
        // 此时 user 对象的 password 字段还是 null
        String password = userDTO.getPassword();
        // 2. 从 DTO 中取出前端传来的【明文密码】
        String salt = userDTO.getSalt();
        // 3. 从 DTO 中取出生成的【随机盐值】
        String md5Passward= DigestUtils.md5DigestAsHex((password+salt).getBytes());
        // 4. 【核心安全操作】：将 明文密码 + 盐值 进行 MD5 哈希计算
        // 这样即使数据库泄露，黑客也无法直接看到原始密码
        user.setPassword(md5Passward);
        // 5. 将计算出的【加密密文】设置到 User 实体中
        userRepository.save(user);
        // 6. 最终将包含【加密密码】的实体保存到数据库
        return user.getUser_id();
    }

    @Override
    public String login(String userName, String password) {
        User user=userRepository.findByUserName(userName)
                .orElseThrow(()->new IllegalArgumentException("userName:" + userName + " not found"));

        String md5Password= DigestUtils.md5DigestAsHex((password+user.getSalt()).getBytes());
        if(!md5Password.equals(user.getPassword())){
            throw new IllegalArgumentException("username and password not match");
        }
        return md5Password;
    }
}
