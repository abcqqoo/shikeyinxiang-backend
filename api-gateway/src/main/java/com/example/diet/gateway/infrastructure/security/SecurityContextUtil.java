package com.example.diet.gateway.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

/**
 * Security 上下文工具类
 */
public final class SecurityContextUtil {

    private SecurityContextUtil() {
        // 工具类禁止实例化
    }

    /**
     * 获取当前认证用户 ID
     */
    public static Long getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof Long)
                .map(principal -> (Long) principal)
                .orElse(null);
    }

    /**
     * 获取当前认证用户名
     */
    public static String getCurrentUsername() {
        return getDetailValue("username");
    }

    /**
     * 获取当前认证用户角色
     */
    public static String getCurrentRole() {
        return getDetailValue("role");
    }

    /**
     * 获取当前 Token
     */
    public static String getCurrentToken() {
        return getDetailValue("token");
    }

    /**
     * 检查当前用户是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Long;
    }

    /**
     * 检查当前用户是否是管理员
     */
    public static boolean isAdmin() {
        String role = getCurrentRole();
        return "ADMIN".equals(role) || "ROLE_ADMIN".equals(role);
    }

    @SuppressWarnings("unchecked")
    private static String getDetailValue(String key) {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getDetails)
                .filter(details -> details instanceof Map)
                .map(details -> (Map<String, Object>) details)
                .map(map -> map.get(key))
                .map(Object::toString)
                .orElse(null);
    }
}
