package com.example.diet.gateway.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT 工具类
 * 负责 Token 的生成、解析、验证和黑名单管理
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";

    private final JwtProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final SecretKey secretKey;

    public JwtUtil(JwtProperties properties, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param claims  自定义声明
     * @param subject 主题 (通常是用户名)
     * @return JWT Token
     */
    public String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + properties.getExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 JWT Token
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true 如果有效
     */
    public boolean validateToken(String token) {
        try {
            // 检查黑名单
            if (isTokenBlacklisted(token)) {
                log.debug("Token is blacklisted");
                return false;
            }

            // 解析并验证
            Claims claims = parseToken(token);

            // 检查是否过期
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.debug("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将 Token 加入黑名单
     *
     * @param token JWT Token
     */
    public void blacklistToken(String token) {
        try {
            Claims claims = parseToken(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                String key = TOKEN_BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
                log.debug("Token added to blacklist, TTL: {} ms", ttl);
            }
        } catch (JwtException e) {
            log.warn("Failed to blacklist token: {}", e.getMessage());
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token JWT Token
     * @return true 如果在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中获取角色
     */
    public String getRole(String token) {
        return (String) parseToken(token).get("role");
    }
}
