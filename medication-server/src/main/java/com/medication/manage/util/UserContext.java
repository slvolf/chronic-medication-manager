package com.medication.manage.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前用户工具类
 * 从 SecurityContext 中获取当前登录用户 ID
 */
public class UserContext {

    /**
     * 获取当前登录用户 ID
     * @return 用户ID（未登录时返回 null）
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
