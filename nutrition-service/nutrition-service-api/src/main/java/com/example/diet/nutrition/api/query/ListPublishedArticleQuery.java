package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询已发布文章列表（小程序端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPublishedArticleQuery implements Query {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;
}
