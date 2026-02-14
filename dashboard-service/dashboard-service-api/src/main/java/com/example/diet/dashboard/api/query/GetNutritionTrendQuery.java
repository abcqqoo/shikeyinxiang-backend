package com.example.diet.dashboard.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取营养趋势查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNutritionTrendQuery implements Query {

    /**
     * 统计周期: week/month/year
     */
    @Builder.Default
    private String period = "week";
}
