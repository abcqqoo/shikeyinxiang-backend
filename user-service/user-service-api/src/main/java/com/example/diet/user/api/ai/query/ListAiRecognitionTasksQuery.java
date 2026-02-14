package com.example.diet.user.api.ai.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 分页查询 AI 识别任务参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListAiRecognitionTasksQuery implements Query {

    /**
     * 页码（从 1 开始）
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页条数
     */
    @Builder.Default
    private Integer pageSize = 10;

    /**
     * 状态筛选：completed/failed/pending/cancelled
     */
    private String status;

    /**
     * 开始日期（含）
     */
    private LocalDate startDate;

    /**
     * 结束日期（含）
     */
    private LocalDate endDate;
}
