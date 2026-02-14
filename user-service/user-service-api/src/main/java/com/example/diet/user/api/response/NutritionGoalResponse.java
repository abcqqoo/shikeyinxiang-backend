package com.example.diet.user.api.response;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 营养目标响应
 */
@Data
public class NutritionGoalResponse implements Serializable {

    private Long id;

    private Long userId;

    /**
     * 每日卡路里目标 (kcal)
     */
    private BigDecimal calorieTarget;

    /**
     * 每日蛋白质目标 (g)
     */
    private BigDecimal proteinTarget;

    /**
     * 每日脂肪目标 (g)
     */
    private BigDecimal fatTarget;

    /**
     * 每日碳水目标 (g)
     */
    private BigDecimal carbsTarget;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
