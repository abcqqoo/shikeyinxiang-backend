package com.example.diet.user.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 更新营养目标命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNutritionGoalCommand implements Command {

    private Long userId;

    /**
     * 每日卡路里目标 (kcal)
     */
    @Positive(message = "卡路里目标必须为正数")
    private BigDecimal calorieTarget;

    /**
     * 每日蛋白质目标 (g)
     */
    @Positive(message = "蛋白质目标必须为正数")
    private BigDecimal proteinTarget;

    /**
     * 每日脂肪目标 (g)
     */
    @Positive(message = "脂肪目标必须为正数")
    private BigDecimal fatTarget;

    /**
     * 每日碳水目标 (g)
     */
    @Positive(message = "碳水目标必须为正数")
    private BigDecimal carbsTarget;
}
