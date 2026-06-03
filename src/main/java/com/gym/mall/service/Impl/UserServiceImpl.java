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
        String token = jwtUtils.getToken(user.getUser_id().toString(), user.getUserName(), user.getRole());
        
        // 5. 返回 Token 给前端，前端后续请求都要带上它
        return token;
    }
}
