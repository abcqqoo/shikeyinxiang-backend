package com.example.diet.food.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询食物列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListFoodsQuery implements Query {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    /**
     * 分类ID筛选
     */
    private Long categoryId;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sortBy = "id";

    /**
     * 排序方向
     */
    @Builder.Default
    private String sortOrder = "asc";
}
