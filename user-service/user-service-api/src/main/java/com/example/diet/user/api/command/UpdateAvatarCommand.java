package com.example.diet.user.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新头像命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvatarCommand implements Command {

    private Long userId;

    @NotBlank(message = "头像URL不能为空")
    private String avatarUrl;
}
