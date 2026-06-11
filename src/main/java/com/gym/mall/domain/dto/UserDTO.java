package com.gym.mall.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "用户信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String userName;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "角色：USER / ADMIN")
    private String role;

    @JsonIgnore
    @Schema(hidden = true)
    private String salt = UUID.randomUUID().toString().replaceAll("-", "");
}
