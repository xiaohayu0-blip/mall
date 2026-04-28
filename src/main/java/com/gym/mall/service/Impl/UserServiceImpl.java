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
        String password = userDTO.getPassword();
        String salt = userDTO.getSalt();
        String md5Passward= DigestUtils.md5DigestAsHex((password+salt).getBytes());
        user.setPassword(md5Passward);
        userRepository.save(user);
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
