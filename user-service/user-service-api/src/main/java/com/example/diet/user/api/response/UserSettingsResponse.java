package com.example.diet.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置响应.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Boolean allowDataAnalysis;
    private Boolean allowPersonalization;
    private LocalDateTime updatedAt;
}
