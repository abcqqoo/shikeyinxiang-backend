package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取文章详情查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetArticleDetailQuery implements Query {

    @NotNull(message = "文章ID不能为空")
    private Long id;

    @Builder.Default
    private Boolean incrementView = false;
}
