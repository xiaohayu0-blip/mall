package com.gym.mall.service;

import com.gym.mall.Repository.UserRepository;
import com.gym.mall.converter.UserConverter;
import com.gym.mall.domain.dto.UserDTO;
import com.gym.mall.domain.entity.User;
import com.gym.mall.service.Impl.UserServiceImpl;
import com.gym.mall.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO registerDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new UserDTO();
        registerDTO.setUserName("test_user");
        registerDTO.setPassword("123456");
    }

    @Test
    @DisplayName("注册 - Converter 正确生成密码和盐值")
    void register_converterShouldHashPassword() {
        User converted = UserConverter.convertToUser(registerDTO);

        assertThat(converted.getPassword()).isNotBlank();
        assertThat(converted.getSalt()).isNotBlank();
        assertThat(converted.getRole()).isEqualTo("USER");
        // 密码不能是明文
        assertThat(converted.getPassword()).isNotEqualTo("123456");
    }

    @Test
    @DisplayName("注册 - Service 保存并返回 ID")
    void register_shouldSaveAndReturnId() {
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    return User.builder()
                            .userId(42L)
                            .userName(u.getUserName())
                            .password(u.getPassword())
                            .salt(u.getSalt())
                            .role(u.getRole())
                            .build();
                });

        Long userId = userService.registerUser(registerDTO);

        assertThat(userId).isEqualTo(42L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("登录 - 正确凭证返回 Token")
    void login_validCredentials_returnsToken() {
        // 准备用户数据
        User user = User.builder()
                .userId(1L)
                .userName("test_user")
                .role("USER")
                .salt("test_salt")
                .password(DigestUtils.md5DigestAsHex(("123456" + "test_salt").getBytes()))
                .build();

        when(userRepository.findByUserName("test_user")).thenReturn(Optional.of(user));
        when(jwtUtils.getToken(eq("1"), eq("test_user"), eq("USER")))
                .thenReturn("mock.jwt.token");

        String token = userService.login("test_user", "123456");

        assertThat(token).isEqualTo("mock.jwt.token");
    }

    @Test
    @DisplayName("登录 - 用户名不存在抛出异常")
    void login_userNotFound_throwsException() {
        when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("unknown", "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("登录 - 密码错误抛出异常")
    void login_wrongPassword_throwsException() {
        User user = User.builder()
                .userId(1L)
                .userName("test_user")
                .salt("test_salt")
                .password("correct_hashed_password")
                .build();

        when(userRepository.findByUserName("test_user")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login("test_user", "wrong_password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("密码错误");
    }
}
