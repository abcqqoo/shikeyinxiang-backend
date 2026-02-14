package com.example.diet.food.domain.model;

import com.example.diet.shared.ddd.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 营养信息值对象
 */
@Getter
@EqualsAndHashCode
public class NutritionInfo implements ValueObject {

    private final BigDecimal calories;
    private final BigDecimal protein;
    private final BigDecimal fat;
    private final BigDecimal saturatedFat;
    private final BigDecimal carbs;
    private final String fiber;

    private NutritionInfo(BigDecimal calories, BigDecimal protein, BigDecimal fat,
                          BigDecimal saturatedFat, BigDecimal carbs, String fiber) {
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.saturatedFat = saturatedFat;
        this.carbs = carbs;
        this.fiber = fiber;
    }

    public static NutritionInfo of(BigDecimal calories, BigDecimal protein, BigDecimal fat,
                                   BigDecimal saturatedFat, BigDecimal carbs, String fiber) {
        return new NutritionInfo(calories, protein, fat, saturatedFat, carbs, fiber);
    }

    /**
     * 计算总营养素 (蛋白质 + 脂肪 + 碳水)
     */
    public BigDecimal totalMacros() {
        return protein.add(fat).add(carbs);
    }
}
