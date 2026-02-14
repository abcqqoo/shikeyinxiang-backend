package com.example.diet.record.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询常用餐模板.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListCommonMealTemplatesQuery implements Query {

    private Long userId;

    /**
     * 可选：餐次类型（breakfast/lunch/dinner/snacks）。
     */
    private String mealType;

    /**
     * 返回条数，默认 6，最大 20。
     */
    @Builder.Default
    private Integer limit = 6;

    /**
     * 统计最近天数，默认 30，最大 90。
     */
    @Builder.Default
    private Integer recentDays = 30;
}
