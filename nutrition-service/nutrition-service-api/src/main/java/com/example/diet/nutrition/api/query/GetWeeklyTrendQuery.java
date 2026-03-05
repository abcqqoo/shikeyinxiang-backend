package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 获取周营养趋势查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetWeeklyTrendQuery implements Query {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 查询周的某一天 (默认当天所在周)
     */
    private LocalDate date;
}
