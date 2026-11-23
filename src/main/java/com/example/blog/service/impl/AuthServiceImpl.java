package com.example.blog.service.impl;

import com.example.blog.common.ResultCode;
import com.example.blog.dto.request.LoginRequest;
import com.example.blog.dto.request.RegisterRequest;
import com.example.blog.dto.response.LoginResponse;
import com.example.blog.dto.response.UserVO;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.AuthService;
import com.example.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证逻辑实现。
 *
 * 两个值得注意的设计：
 * 1. 登录失败提示统一为"用户名或密码错误"——不区分"用户不存在"和"密码错误"，
 *    否则等于向攻击者泄露"哪些用户名是注册过的"
 * 2. @RequiredArgsConstructor 生成 final 字段的构造器，配合 Spring 的构造器注入：
 *    字段不可变、依赖显式、不依赖字段注入
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserVO register(RegisterRequest request) {
        // 业务校验：查库的规则放这里，注解做不了
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已被占用");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // 密文入库：从这一刻起，数据库里永远不存在明文密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null || request.getNickname().isBlank()
                ? request.getUsername() : request.getNickname());
        user.setAvatar("");

        userMapper.insert(user);
        log.info("新用户注册: id={}, username={}", user.getId(), user.getUsername());

        // 注意：insert 回填了自增 id，但 created_at 是数据库默认值生成的，
        // 这个内存对象上还是 null。要么再查一次，要么前端接受 createTime 缺省
        return UserVO.from(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        // 用户不存在和密码错误返回同一个提示，不泄露用户名存在性
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }

        String token = jwtUtil.generate(user.getId(), user.getUsername());
        log.info("用户登录: id={}, username={}", user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .user(UserVO.from(user))
                .build();
    }
}
