package com.example.blog.common;

/**
 * 统一错误码枚举。
 * 为什么不用纯数字魔法值：错误码集中定义，前后端可以对着同一张表写错误处理，
 * 新增错误时也有唯一入口，避免各处随手编一个 code。
 */
public enum ResultCode {

    SUCCESS(0, "ok"),

    // 4xx：客户端的问题（参数错、没登录、没权限、资源不存在）
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限执行此操作"),
    NOT_FOUND(404, "请求的资源不存在"),
    CONFLICT(409, "资源状态冲突"),

    // 5xx：服务器的问题
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
