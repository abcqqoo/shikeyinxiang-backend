package com.example.diet.food.domain.model;

import com.example.diet.shared.ddd.Identifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 食物ID值对象
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class FoodId extends Identifier<Long> {

    private FoodId(Long value) {
        super(value);
    }

    public static FoodId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("FoodId must be positive");
        }
        return new FoodId(value);
    }
}
