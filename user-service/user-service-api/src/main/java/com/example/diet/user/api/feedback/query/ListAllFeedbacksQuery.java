package com.example.diet.user.api.feedback.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询所有反馈列表 (管理员)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListAllFeedbacksQuery implements Query {

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

    // ===== 筛选条件 =====

    /**
     * 状态: 0=待处理, 1=处理中, 2=已解决, 3=已关闭
     */
    private Integer status;

    /**
     * 关键字 (内容模糊匹配)
     */
    private String keyword;

    /**
     * 用户ID (可选筛选)
     */
    private Long userId;
}
