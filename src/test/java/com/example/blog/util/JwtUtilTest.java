package com.example.blog.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JWT 工具的纯单元测试：不依赖 Spring，直接 new 被测对象。
 * 这也验证了一个设计点——JwtUtil 不依赖容器，new 出来就能用，好测。
 */
class JwtUtilTest {

    private static final String SECRET = "unit-test-secret-0123456789abcdef0123456789abcdef";

    @Test
    @DisplayName("签发的 token 能解析回正确的 userId")
    void generateAndParseRoundTrip() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 1);

        String token = jwtUtil.generate(42L, "alice");
        Claims claims = jwtUtil.parse(token);

        assertEquals("42", claims.getSubject());
        assertEquals("alice", claims.get("username", String.class));
        assertEquals(42L, jwtUtil.parseUserId(token));
    }

    @Test
    @DisplayName("有效期 0 小时的 token 立即过期")
    void expiredTokenShouldThrow() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 0);
        String token = jwtUtil.generate(1L, "bob");

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.parse(token));
        assertNull(jwtUtil.parseUserId(token), "过期的 token 解析应得到 null 而不是抛出");
    }

    @Test
    @DisplayName("用别的密钥签的 token 无法通过校验")
    void tamperedTokenShouldThrow() {
        JwtUtil issuer = new JwtUtil(SECRET, 1);
        JwtUtil verifier = new JwtUtil(SECRET + "-different", 1);

        String token = issuer.generate(1L, "alice");

        assertThrows(JwtException.class, () -> verifier.parse(token));
    }
}
