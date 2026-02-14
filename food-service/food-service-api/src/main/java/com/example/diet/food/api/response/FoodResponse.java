package com.example.diet.food.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 食物响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String measure;
    private BigDecimal grams;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal saturatedFat;
    private BigDecimal carbs;
    private String fiber;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
}
