package com.example.diet.user.api.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户设置命令.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserSettingsCommand implements Command {

    private Long userId;
    private Boolean allowDataAnalysis;
    private Boolean allowPersonalization;
}
