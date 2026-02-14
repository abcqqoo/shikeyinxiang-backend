package com.example.diet.record.infrastructure.persistence.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户每日营养汇总 DTO
 */
@Data
public class UserDailyNutritionDTO {
    private Long userId;
    private LocalDate recordDate;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbs;
}
