package com.example.diet.user.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户状态枚举
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {

    DISABLED(0, "禁用"),
    ACTIVE(1, "启用");

    private final int value;
    private final String displayName;

    public static UserStatus of(Integer value) {
        if (value == null) {
            return ACTIVE;
        }
        for (UserStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return ACTIVE;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
