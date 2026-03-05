package com.example.diet.nutrition.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Health report response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Integer totalDays;
    private Integer recordDays;
    private Integer lastWeekRecordDays;
    private Integer healthScore;
    private Integer scoreChange;
    private NutritionBalance nutritionBalance;
    private WeeklyProgress weeklyProgress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionBalance implements Serializable {
        private static final long serialVersionUID = 1L;

        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyProgress implements Serializable {
        private static final long serialVersionUID = 1L;

        private NutrientProgress calorie;
        private NutrientProgress protein;
        private NutrientProgress carbs;
        private NutrientProgress fat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutrientProgress implements Serializable {
        private static final long serialVersionUID = 1L;

        private BigDecimal lastWeek;
        private BigDecimal thisWeek;
    }
}
