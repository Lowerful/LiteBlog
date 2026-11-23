package com.example.blog.service;

import com.example.blog.dto.request.LoginRequest;
import com.example.blog.dto.request.RegisterRequest;
import com.example.blog.dto.response.LoginResponse;
import com.example.blog.dto.response.UserVO;

/**
 * 认证服务：注册与登录。
 *
 * 接口 + 实现分离的意义：Controller 依赖接口而非实现类，
 * 以后换实现（比如换成手机号登录）不影响调用方；也方便测试时 mock。
 */
public interface AuthService {

    UserVO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
