package com.example.blog.util;

import com.example.blog.exception.BusinessException;
import com.example.blog.common.ResultCode;

/**
 * 当前登录用户上下文。
 *
 * 原理：Tomcat 的每个请求由一个独立线程处理，拦截器在校验完 JWT 后把 userId
 * 放进该线程的 ThreadLocal，之后本次请求内任何代码都能取到"我是谁"，
 * 不用一层层传参。请求结束后必须 clear()（拦截器的 afterCompletion 里做），
 * 否则 Tomcat 复用线程时会把上一个用户的身份带给下一个请求——严重事故。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    /** 业务代码里"必须已登录"的场景用它：没登录直接抛 401 */
    public static Long requireUserId() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
