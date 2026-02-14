package com.example.diet.record.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 全站营养趋势汇总响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTrendSummaryResponse implements Serializable {

    private List<String> dateList;
    private List<BigDecimal> calorieList;
    private List<BigDecimal> proteinList;
    private List<BigDecimal> carbsList;
    private List<BigDecimal> fatList;
    private List<Long> activeUserCountList;
}
