package com.example.diet.nutrition.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 营养统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionStatsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private int days;
    private BigDecimal avgCalories;
    private BigDecimal avgProtein;
    private BigDecimal avgFat;
    private BigDecimal avgCarbs;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbs;
}
