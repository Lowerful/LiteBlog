package com.example.blog.controller;

import com.example.blog.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口。
 *
 * 用途：骨架搭好后先跑通"请求 -> 响应"链路，验证统一返回结构确实生效。
 * 这也是真实项目里最经典的第一个接口（DevOps 的探活也靠它）。
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Result<String> health() {
        return Result.ok("LiteBlog is running");
    }
}
