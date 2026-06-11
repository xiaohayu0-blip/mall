package com.gym.mall.controller;

import com.gym.mall.domain.dto.UserDTO;
import com.gym.mall.validator.Response;
import com.gym.mall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户", description = "用户注册与登录")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "注册用户")
    @PostMapping("/user")
    public Response<Long> registryUser(@Valid @RequestBody UserDTO userDTO) {
        return Response.newSuccess(userService.registerUser(userDTO));
    }

    @Operation(summary = "用户登录", description = "返回 JWT Token")
    @PostMapping("/user/login")
    public Response<String> login(
            @Parameter(description = "用户名") @RequestParam String userName,
            @Parameter(description = "密码") @RequestParam String password) {
        return Response.newSuccess(userService.login(userName, password));
    }
}
