package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.dto.response.UserVO;
import com.example.blog.service.UserService;
import com.example.blog.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。/users/me 在拦截器保护范围内，走到这里说明一定已登录，
 * 所以直接从 UserContext 取身份即可。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(userService.getUserById(UserContext.requireUserId()));
    }
}
