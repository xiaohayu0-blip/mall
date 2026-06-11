package com.gym.mall.service.Impl;

import com.gym.mall.Repository.UserRepository;
import com.gym.mall.converter.UserConverter;
import com.gym.mall.domain.entity.User;
import com.gym.mall.domain.dto.UserDTO;
import com.gym.mall.service.UserService;
import com.gym.mall.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public long registerUser(UserDTO userDTO) {
        // Converter 内部已完成密码加盐 + MD5 加密，直接保存即可
        User user = UserConverter.convertToUser(userDTO);
        userRepository.save(user);
        return user.getUserId();
    }

    @Override
    public String login(String userName, String password) {
        // 1. 根据用户名查询数据库中的用户
        User user=userRepository.findByUserName(userName)
                .orElseThrow(()->new IllegalArgumentException("用户名:" + userName + " 不存在"));

        // 2. 将前端传来的明文密码进行 MD5 加密（注意要加上数据库里存的盐值）
        String md5Password= DigestUtils.md5DigestAsHex((password+user.getSalt()).getBytes());
        
        // 3. 校验加密后的密码是否与数据库一致
        if(!md5Password.equals(user.getPassword())){
            throw new IllegalArgumentException("用户名或密码错误");
        }
        
        // 4. 登录成功，为该用户生成一个 JWT Token
        String token = jwtUtils.getToken(user.getUserId().toString(), user.getUserName(), user.getRole());
        
        // 5. 返回 Token 给前端，前端后续请求都要带上它
        return token;
    }
}
