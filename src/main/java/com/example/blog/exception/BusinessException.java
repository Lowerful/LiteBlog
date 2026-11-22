package com.example.blog.exception;

import com.example.blog.common.ResultCode;

/**
 * 业务异常：表示"业务规则不满足"（用户名已存在、文章不属于你……）。
 *
 * 两个设计决定：
 * 1. 继承 RuntimeException 而不是 Exception —— 受检异常会强迫每一层方法签名都 throws，
 *    业务代码会被污染；运行时异常配合全局处理器可以"抛了就走"。
 * 2. 异常自带 ResultCode —— 抛异常的人最清楚这是什么性质的错误，
 *    由他来分类，由全局处理器统一翻译成响应。
 */
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /** 允许覆盖默认文案，比如"用户名已被占用"比笼统的"资源冲突"更友好 */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
