package com.example.diet.user.api.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户响应
 */
@Data
public class UserResponse implements Serializable {

    private Long id;

    private String username;

    private String email;

    /**
     * 角色: USER, ADMIN
     */
    private String role;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;

    private String avatarUrl;

    /**
     * 微信 OpenID
     */
    private String openid;

    private LocalDateTime createTime;
}
