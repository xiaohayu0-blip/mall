package com.gym.mall.service;

import com.gym.mall.domain.dto.UserDTO;

public interface UserService {

    long registerUser(UserDTO userDTO);

    String login(String username, String password);

}
