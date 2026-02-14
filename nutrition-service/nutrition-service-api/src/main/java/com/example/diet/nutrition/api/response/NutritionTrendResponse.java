package com.example.diet.nutrition.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Nutrition trend response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTrendResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> dateList;
    private List<BigDecimal> calorieList;
    private List<BigDecimal> proteinList;
    private List<BigDecimal> carbsList;
    private List<BigDecimal> fatList;
}
