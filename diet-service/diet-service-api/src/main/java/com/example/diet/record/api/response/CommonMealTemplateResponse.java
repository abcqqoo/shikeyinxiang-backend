package com.example.diet.record.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 常用餐模板响应.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonMealTemplateResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String templateId;
    private String templateName;
    private String mealType;
    private Integer useCount;
    private LocalDate lastUsedDate;
    private LocalTime lastUsedTime;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbs;
    private List<TemplateFood> foods;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateFood implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long foodId;
        private String foodName;
        private BigDecimal amount;
        private String unit;
        private BigDecimal grams;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal fat;
        private BigDecimal carbs;
        private String source;
    }
}
