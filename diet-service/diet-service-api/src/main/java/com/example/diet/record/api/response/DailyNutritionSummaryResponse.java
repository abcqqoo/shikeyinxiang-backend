package com.example.diet.record.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日营养汇总响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyNutritionSummaryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private LocalDate date;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbs;
    private int recordCount;

    /**
     * 目标卡路里 (来自用户营养目标)
     */
    private BigDecimal calorieTarget;

    /**
     * 卡路里完成百分比
     */
    private BigDecimal caloriePercentage;
}
