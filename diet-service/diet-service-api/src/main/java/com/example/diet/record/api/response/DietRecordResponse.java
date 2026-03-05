package com.example.diet.record.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 饮食记录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietRecordResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String username;
    private LocalDate date;
    private LocalTime time;
    private String mealType;
    private String remark;
    /**
     * regular / recipe
     */
    private String recordType;
    private BigDecimal totalCalories;
    private BigDecimal totalCalorie;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbs;
    private List<DietRecordFoodResponse> foods;
    private String recipeName;
    private String ingredients;
    private String instructions;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
