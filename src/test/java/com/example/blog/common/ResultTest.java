package com.example.blog.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 第一个单元测试。
 *
 * 注意：它没有 @SpringBootTest —— 单元测试只测纯逻辑，不需要启动整个 Spring 容器，
 * 所以跑得飞快。以后需要连数据库/起 HTTP 的测试（集成测试）才会用 @SpringBootTest。
 */
class ResultTest {

    @Test
    @DisplayName("ok() 应返回 code=0 且携带数据")
    void okShouldCarryData() {
        Result<String> result = Result.ok("hello");

        assertEquals(0, result.getCode());
        assertEquals("ok", result.getMessage());
        assertEquals("hello", result.getData());
    }

    @Test
    @DisplayName("fail() 应返回指定错误码和消息，data 为 null")
    void failShouldCarryError() {
        Result<Void> result = Result.fail(ResultCode.NOT_FOUND);

        assertEquals(404, result.getCode());
        assertEquals("请求的资源不存在", result.getMessage());
        assertNull(result.getData());
    }
}
