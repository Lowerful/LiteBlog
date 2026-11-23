package com.example.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密器。
 *
 * 为什么是 BCrypt 而不是 MD5/SHA-256：
 * 1. 自带随机盐——同样的密码每次加密结果都不同，彩虹表失效
 * 2. 刻意慢（可调强度因子，默认 10 轮 ≈ 100ms）——暴力破解成本极高
 * 3. 验证用 matches(明文, 密文)，盐嵌在密文里，不需要单独存储
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
