package com.gym.mall.controller;

import com.gym.mall.dto.UserDTO;
import com.gym.mall.validator.Response;
import com.gym.mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user")
    public Response<Long> registryUser(@RequestBody UserDTO userDTO){
        return Response.newSuccess(userService.registerUser(userDTO));
    }

    @PostMapping("/user/login")
    public Response<String> login(@RequestParam String userName, @RequestParam String password){
        return Response.newSuccess(userService.login(userName,password));
    }
}
