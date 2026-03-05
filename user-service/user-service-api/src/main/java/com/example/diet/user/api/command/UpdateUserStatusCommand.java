package com.example.diet.user.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户状态命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusCommand implements Command {

    private Long userId;

    /**
     * 状态: 1-启用, 0-禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
