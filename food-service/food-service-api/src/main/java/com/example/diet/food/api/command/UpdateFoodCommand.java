package com.example.diet.food.api.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 更新食物命令
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoodCommand implements Command {

    private Long foodId;

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
    private String imageUrl;
}
