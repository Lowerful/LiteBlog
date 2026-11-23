package com.example.blog.interceptor;

import com.example.blog.exception.BusinessException;
import com.example.blog.common.ResultCode;
import com.example.blog.util.JwtUtil;
import com.example.blog.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：所有受保护接口的第一道门。
 *
 * 请求到达 Controller 之前执行 preHandle：
 * 1. 校验 Authorization 头里的 JWT
 * 2. 校验通过 → 把 userId 放进 UserContext，Controller/Service 随取随用
 * 3. afterCompletion（请求彻底结束）→ clear，防止线程复用串号
 *
 * 抛出的 BusinessException 会被 GlobalExceptionHandler 接住，统一返回 401。
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只拦截"会执行业务方法"的请求；静态资源等 handler 不是 HandlerMethod，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        Long userId = jwtUtil.parseUserId(token);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }

        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
