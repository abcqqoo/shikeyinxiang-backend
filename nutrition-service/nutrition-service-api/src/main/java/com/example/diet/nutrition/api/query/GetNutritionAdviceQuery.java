package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取营养建议查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNutritionAdviceQuery implements Query {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private java.time.LocalDate date;
}
