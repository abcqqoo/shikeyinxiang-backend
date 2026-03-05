package com.example.diet.user.api.feedback.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除反馈命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFeedbackCommand implements Command {

    /**
     * 反馈ID
     */
    @NotNull(message = "反馈ID不能为空")
    private Long id;
}
