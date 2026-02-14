package com.example.diet.gateway.infrastructure.security;

import com.example.diet.gateway.infrastructure.jwt.JwtUtil;
import com.example.diet.observability.trace.TraceMdcKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证过滤器
 * 验证 JWT Token 并设置 Spring Security 认证上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 如果没有 Authorization header 或不是 Bearer Token，直接放行
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());

        try {
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);

                // 确保角色有 ROLE_ 前缀
                String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                // 创建认证令牌 - 使用 userId 作为 principal
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix))
                        );

                // 设置额外详情
                Map<String, Object> details = new HashMap<>();
                details.put("username", username);
                details.put("role", role);
                authentication.setDetails(details);

                // 设置 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute(TraceMdcKeys.REQUEST_USER_ID_ATTRIBUTE, userId);
                MDC.put(TraceMdcKeys.USER_ID, String.valueOf(userId));

                log.debug("User authenticated: userId={}, username={}, role={}", userId, username, role);
            }
        } catch (Exception e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
            // 认证失败不抛异常，让 Spring Security 处理未认证情况
        }

        filterChain.doFilter(request, response);
    }
}
