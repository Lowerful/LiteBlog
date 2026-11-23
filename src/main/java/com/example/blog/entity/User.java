package com.example.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体：与 user 表一一对应。
 *
 * 注意它只在 Service 与 Mapper 之间流转，绝不直接返回给前端——
 * 因为它带着 password 字段。对外一律用 dto 包里的 VO。
 */
@Data
public class User {

    private Long id;

    private String username;

    /** BCrypt 密文，永远不要在任何日志或响应里出现 */
    private String password;

    private String nickname;

    private String avatar;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
