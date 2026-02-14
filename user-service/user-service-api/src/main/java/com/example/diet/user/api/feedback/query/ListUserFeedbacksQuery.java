package com.example.diet.user.api.feedback.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询用户自己的反馈列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUserFeedbacksQuery implements Query {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 页码 (从 1 开始)
     */
    @Builder.Default
    private int page = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private int size = 10;
}
