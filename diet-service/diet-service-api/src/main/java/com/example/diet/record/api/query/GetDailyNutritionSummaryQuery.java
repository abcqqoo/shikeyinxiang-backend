package com.example.diet.record.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 获取每日营养汇总查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDailyNutritionSummaryQuery implements Query {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "日期不能为空")
    private LocalDate date;
}
