package com.example.diet.food.domain.model;

import com.example.diet.shared.ddd.Identifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 食物分类ID值对象
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class FoodCategoryId extends Identifier<Long> {

    private FoodCategoryId(Long value) {
        super(value);
    }

    public static FoodCategoryId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("FoodCategoryId must be positive");
        }
        return new FoodCategoryId(value);
    }
}
