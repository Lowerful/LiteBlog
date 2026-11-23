package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求。
 *
 * 校验分两层：这里的注解管"格式"（非空、长度、字符规则），
 * "用户名是否已被占用"这类要查库的规则在 Service 里做——分工见校验分层原则。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能是 4-20 位字母、数字或下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在 6-32 位之间")
    private String password;

    /** 可选：不填则默认用用户名 */
    @Size(max = 32, message = "昵称最长 32 个字符")
    private String nickname;
}
