package com.example.diet.file.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预签名URL响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 预签名URL
     */
    private String url;

    /**
     * 文件Key
     */
    private String key;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}
