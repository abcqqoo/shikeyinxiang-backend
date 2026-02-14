package com.example.diet.user.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建用户命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserCommand implements Command {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20 个字符")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(min = 8, max = 32, message = "密码长度 8-32 个字符")
    private String password;

    private String avatarUrl;

    /**
     * 用户角色: USER, ADMIN
     */
    @Builder.Default
    private String role = "USER";

    /**
     * 微信 OpenID (微信登录时使用)
     */
    private String openid;
}
