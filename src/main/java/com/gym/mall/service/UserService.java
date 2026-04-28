package com.gym.mall.service;

import com.gym.mall.dto.UserDTO;

public interface UserService {

    long registerUser(UserDTO userDTO);

    String login(String username, String password);

}
