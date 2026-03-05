package com.example.diet.food.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取食物分类查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetFoodCategoryQuery implements Query {

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}
