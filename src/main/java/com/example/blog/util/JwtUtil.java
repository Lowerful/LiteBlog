package com.example.blog.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发与校验。
 *
 * JWT 的结构是三段用 . 连接的 Base64：header.payload.signature。
 * payload 只是 Base64 编码，任何人都能解码看到内容——所以绝不能放敏感信息，
 * 只放"谁"（userId）。签名保证 payload 没被篡改。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-hours:72}") long expireHours) {
        // HS256 要求密钥至少 32 字节，太短的密钥在 Keys.hmacShaKeyFor 里会直接报错
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs = expireHours * 3600 * 1000;
    }

    /** 签发 token：subject 放 userId，这是解析时要拿回的核心信息 */
    public String generate(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMs))
                .signWith(key)
                .compact();
    }

    /** 校验并解析，返回全部声明。签名不对或已过期会抛 JwtException */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 便捷方法：只取 userId。校验失败统一抛出，由全局处理器翻译成 401 */
    public Long parseUserId(String token) {
        try {
            return Long.valueOf(parse(token).getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
