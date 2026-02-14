package com.example.diet.user.api.feedback.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取单个反馈详情查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetFeedbackQuery implements Query {

    /**
     * 反馈ID
     */
    @NotNull(message = "反馈ID不能为空")
    private Long id;

    /**
     * 用户ID (用于验证权限，可选 - 管理员查看时不需要)
     */
    private Long userId;
}
