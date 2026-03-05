package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询营养建议列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListNutritionAdviceQuery implements Query {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    private String conditionType;

    private Integer status;

    private String keyword;
}
