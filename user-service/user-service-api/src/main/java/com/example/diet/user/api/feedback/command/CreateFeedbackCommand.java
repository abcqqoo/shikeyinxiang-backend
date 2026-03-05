package com.example.diet.user.api.feedback.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建反馈命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeedbackCommand implements Command {

    /**
     * 用户ID (由 Controller 从 JWT 中获取设置)
     */
    private Long userId;

    /**
     * 反馈类型: 0=建议, 1=问题, 2=其他
     */
    @NotNull(message = "反馈类型不能为空")
    private Integer type;

    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    private String content;

    /**
     * 联系方式 (可选)
     */
    private String contact;
}
