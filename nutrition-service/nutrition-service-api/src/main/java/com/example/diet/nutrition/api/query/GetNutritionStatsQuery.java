package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 获取营养统计查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNutritionStatsQuery implements Query {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
}
