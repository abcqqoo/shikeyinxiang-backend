package com.example.diet.dashboard.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取热门食物查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPopularFoodsQuery implements Query {

    /**
     * 统计周期: week/month/year
     */
    @Builder.Default
    private String period = "week";

    /**
     * 返回数量限制
     */
    @Builder.Default
    private Integer limit = 10;
}
