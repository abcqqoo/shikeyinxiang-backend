package com.example.diet.gateway.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {

    /**
     * 用户名 (管理员登录)
     */
    private String username;

    /**
     * 邮箱 (用户登录)
     */
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
