package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Nutrition trend query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNutritionTrendQuery implements Query {
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
}
