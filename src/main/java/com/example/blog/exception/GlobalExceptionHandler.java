package com.example.blog.exception;

import com.example.blog.common.Result;
import com.example.blog.common.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * 全局异常处理器：全项目唯一一个把异常翻译成响应的地方。
 *
 * 工作原理：@RestControllerAdvice + @ExceptionHandler 组成的拦截网，
 * 任何 Controller 抛出的异常都会先到这里，匹配到对应类型就走对应方法，
 * 匹配不到就走最后的 Exception 兜底。
 *
 * 好处：Controller / Service 里再也不需要 try-catch + 手工组装错误返回，
 * 业务代码只负责"发现问题就抛异常"。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：属于预期内的失败，用它的 code 和 message，只记 info 日志 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.info("业务异常: code={}, message={}", e.getResultCode().getCode(), e.getMessage());
        return Result.fail(e.getResultCode().getCode(), e.getMessage());
    }

    /** @Valid 校验请求体（DTO）失败：取第一条错误信息返回给前端 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("请求参数错误");
        log.info("参数校验失败: {}", message);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 校验 Controller 方法参数（@RequestParam / @PathVariable 上直接加约束注解）失败。
     * 注意版本差异：Spring Framework 6.1 起控制器方法参数校验改为内置机制，
     * 失败抛的是 HandlerMethodValidationException，不再是 ConstraintViolationException。
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result<Void> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("请求参数错误");
        log.info("参数校验失败: {}", message);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** 校验普通 Bean（非 Controller 方法参数，如 @Validated 的 Service）的入参失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("请求参数错误");
        log.info("参数校验失败: {}", message);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** 兜底：任何没被预期的异常。记 error 日志（带堆栈），对外只说"服务器内部错误" */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未处理异常", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
