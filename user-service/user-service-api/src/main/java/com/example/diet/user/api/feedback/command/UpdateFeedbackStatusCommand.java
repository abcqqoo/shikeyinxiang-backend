package com.example.diet.user.api.feedback.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新反馈状态命令 (管理员)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedbackStatusCommand implements Command {

    /**
     * 反馈ID
     */
    @NotNull(message = "反馈ID不能为空")
    private Long id;

    /**
     * 状态: 0=待处理, 1=处理中, 2=已解决, 3=已关闭
     */
    private Integer status;

    /**
     * 管理员回复
     */
    private String adminReply;
}
