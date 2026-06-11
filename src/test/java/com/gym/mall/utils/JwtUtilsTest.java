package com.gym.mall.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JWT 工具类单元测试")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 通过反射注入 @Value 字段
        ReflectionTestUtils.setField(jwtUtils, "secretKey", "test_secret_key_for_jwt_test");
        ReflectionTestUtils.setField(jwtUtils, "expireHours", 72L);
    }

    @Test
    @DisplayName("生成 Token 并解析 - 能正确取出所有 Claims")
    void getToken_and_parseToken_shouldMatch() {
        String token = jwtUtils.getToken("42", "admin", "ADMIN");

        assertThat(token).isNotBlank();
        // JWT 由三段用 . 分隔的 base64 编码组成
        assertThat(token.split("\\.")).hasSize(3);

        Map<String, String> claims = jwtUtils.parseToken(token);

        assertThat(claims.get("userId")).isEqualTo("42");
        assertThat(claims.get("userName")).isEqualTo("admin");
        assertThat(claims.get("role")).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("生成 Token 并解析 - 普通用户角色")
    void getToken_and_parseToken_userRole() {
        String token = jwtUtils.getToken("100", "test_user", "USER");

        Map<String, String> claims = jwtUtils.parseToken(token);

        assertThat(claims.get("userId")).isEqualTo("100");
        assertThat(claims.get("userName")).isEqualTo("test_user");
        assertThat(claims.get("role")).isEqualTo("USER");
    }

    @Test
    @DisplayName("解析伪造 Token - 应抛出异常")
    void parseToken_invalidToken_throwsException() {
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.fake.fake";

        assertThatThrownBy(() -> jwtUtils.parseToken(fakeToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("解析被篡改的 Token - 签名校验失败")
    void parseToken_tamperedToken_throwsException() {
        // 用一个有效签名但被篡改 payload 的 token
        String tamperedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI5OTkifQ.invalidsignature";

        assertThatThrownBy(() -> jwtUtils.parseToken(tamperedToken))
                .isInstanceOf(RuntimeException.class);
    }
}
