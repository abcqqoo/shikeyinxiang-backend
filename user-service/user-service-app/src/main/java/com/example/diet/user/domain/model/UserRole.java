package com.example.diet.user.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户角色枚举
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {

    USER("普通用户"),
    ADMIN("管理员");

    private final String displayName;

    public static UserRole of(String value) {
        if (value == null) {
            return USER;
        }
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return USER;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
