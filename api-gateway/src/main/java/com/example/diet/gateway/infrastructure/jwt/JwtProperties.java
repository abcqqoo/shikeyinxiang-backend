package com.example.diet.gateway.infrastructure.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥 (Base64 编码, 至少 256 位)
     */
    private String secret;

    /**
     * Token 有效期 (毫秒), 默认 24 小时
     */
    private long expiration = 86400000L;

    /**
     * Token 刷新时间 (毫秒), 默认 7 天
     */
    private long refreshExpiration = 604800000L;

    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Header 名称
     */
    private String headerName = "Authorization";
}
