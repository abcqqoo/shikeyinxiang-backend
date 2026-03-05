package com.example.diet.record.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取热门食物统计查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPopularFoodStatsQuery implements Query {

    /**
     * 统计周期: week/month/quarter
     */
    @Builder.Default
    private String period = "week";

    /**
     * 返回数量限制
     */
    @Builder.Default
    private Integer limit = 10;
}
