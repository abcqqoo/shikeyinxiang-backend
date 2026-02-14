package com.example.diet.user.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCommand implements Command {

    private Long userId;

    @Size(min = 3, max = 20, message = "用户名长度 3-20 个字符")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatarUrl;
}
