package com.example.blog.interceptor;

import com.example.blog.exception.BusinessException;
import com.example.blog.common.ResultCode;
import com.example.blog.util.JwtUtil;
import com.example.blog.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;

import java.util.List;
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

    /**
     * 公开的文章浏览接口（仅 GET）。
     * 为什么不放 WebMvcConfig 的 excludePathPatterns：那里的白名单只匹配路径、
     * 不匹配 HTTP 方法——把 /api/articles 加进去，PUT/DELETE 也一起被放行了。
     * "同一资源路径、不同方法、不同权限"是 RESTful 的常态，只能在这里按方法判断。
     */
    private static final List<String> PUBLIC_GET_PATTERNS = List.of("/api/articles", "/api/articles/*");
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只拦截"会执行业务方法"的请求；静态资源等 handler 不是 HandlerMethod，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 文章的浏览接口对游客开放，但要"尽力识别身份"：带了合法 token 的请求
        // 依然装配 UserContext——"草稿仅作者可见"这类规则依赖它。
        // 与写接口的区别：这里 token 缺失或无效不拦截，只是不设身份
        if ("GET".equals(request.getMethod()) && isPublicArticlePath(request.getRequestURI())) {
            String bearer = request.getHeader("Authorization");
            if (bearer != null && bearer.startsWith(BEARER_PREFIX)) {
                Long optionalUserId = jwtUtil.parseUserId(bearer.substring(BEARER_PREFIX.length()));
                if (optionalUserId != null) {
                    UserContext.setUserId(optionalUserId);
                }
            }
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

    /** AntPathMatcher：Spring 自带的路径匹配器，支持 * 通配，与 @RequestMapping 用的是同一套规则 */
    private boolean isPublicArticlePath(String uri) {
        return PUBLIC_GET_PATTERNS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, uri));
    }
}
