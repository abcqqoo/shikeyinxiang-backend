package com.example.diet.record.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取全站营养趋势汇总查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNutritionTrendSummaryQuery implements Query {

    /**
     * 统计周期: week/month/year
     */
    @Builder.Default
    private String period = "week";
}
