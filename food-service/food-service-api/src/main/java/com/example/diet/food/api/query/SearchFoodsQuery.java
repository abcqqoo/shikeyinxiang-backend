package com.example.diet.food.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索食物查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFoodsQuery implements Query {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 分类ID筛选
     */
    private Long categoryId;
}
