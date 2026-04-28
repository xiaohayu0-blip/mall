package com.gym.mall.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
//自动生成一个无参构造函数
public class UserDTO {

    private Long id;

    private String userName;

    private String password;

    @JsonIgnore
    private String salt= UUID.randomUUID().toString().replaceAll("-", "");
}
