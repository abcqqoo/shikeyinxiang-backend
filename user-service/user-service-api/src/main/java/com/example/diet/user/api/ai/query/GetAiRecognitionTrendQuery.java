package com.example.diet.user.api.ai.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取 AI 识别趋势查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAiRecognitionTrendQuery implements Query {

    /**
     * 统计周期: week/month/year
     */
    @Builder.Default
    private String period = "month";
}
