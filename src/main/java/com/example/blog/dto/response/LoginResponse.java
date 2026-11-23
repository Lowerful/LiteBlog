package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应：token + 用户信息一起给，前端登录成功后无需再调一次 /users/me。
 */
@Data
@Builder
public class LoginResponse {

    /** 后续所有需登录的请求都要带上：Authorization: Bearer <token> */
    private String token;

    private UserVO user;
}
