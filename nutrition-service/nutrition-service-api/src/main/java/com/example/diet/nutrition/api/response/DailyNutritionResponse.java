package com.example.diet.nutrition.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日营养响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyNutritionResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private int recordCount;
}
