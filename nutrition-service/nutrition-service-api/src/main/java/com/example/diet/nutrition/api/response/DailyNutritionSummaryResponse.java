package com.example.diet.nutrition.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily nutrition summary response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyNutritionSummaryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private BigDecimal calorie;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private BigDecimal caloriePercentage;
    private BigDecimal proteinPercentage;
    private BigDecimal carbsPercentage;
    private BigDecimal fatPercentage;
    private BigDecimal calorieTarget;
    private BigDecimal proteinTarget;
    private BigDecimal carbsTarget;
    private BigDecimal fatTarget;
}
