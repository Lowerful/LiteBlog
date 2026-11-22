package com.example.blog.common;

import lombok.Data;

/**
 * 统一返回结构：所有接口的响应体都是它。
 *
 * 为什么不直接返回业务数据 + HTTP 状态码？
 * 1. HTTP 状态码表达不了业务细节（比如"用户名已被占用"该是什么码？）
 * 2. 前端只需要判断 code === 0 即可走成功分支，错误提示直接用 message
 * 3. data 永远是业务数据本体，结构稳定，前端类型定义好写
 */
@Data
public class Result<T> {

    /** 业务状态码：0 成功，非 0 失败（见 ResultCode） */
    private int code;

    /** 提示信息，成功时为 "ok"，失败时为可直接展示给用户的文案 */
    private String message;

    /** 业务数据本体，失败时通常为 null */
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
