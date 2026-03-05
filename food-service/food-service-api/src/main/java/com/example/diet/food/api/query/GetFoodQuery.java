package com.example.diet.food.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取食物查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetFoodQuery implements Query {

    @NotNull(message = "食物ID不能为空")
    private Long foodId;
}
