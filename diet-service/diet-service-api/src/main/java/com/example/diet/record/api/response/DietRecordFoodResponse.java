package com.example.diet.record.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 饮食记录食物明细响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietRecordFoodResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long foodId;
    private String foodName;
    private String name;
    private BigDecimal amount;
    private String unit;
    private BigDecimal grams;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    /**
     * database / ai_estimated
     */
    private String source;
}
