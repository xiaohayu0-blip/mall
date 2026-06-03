package com.gym.mall.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String userName;

    private String password;

    private String role;

    @JsonIgnore
    private String salt= UUID.randomUUID().toString().replaceAll("-", "");
}
