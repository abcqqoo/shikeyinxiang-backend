package com.example.diet.food.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建食物命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFoodCommand implements Command {

    @NotBlank(message = "食物名称不能为空")
    private String name;

    @NotBlank(message = "份量描述不能为空")
    private String measure;

    @NotNull(message = "克数不能为空")
    @Positive(message = "克数必须为正数")
    private BigDecimal grams;

    @NotNull(message = "卡路里不能为空")
    @Positive(message = "卡路里必须为正数")
    private BigDecimal calories;

    @NotNull(message = "蛋白质不能为空")
    private BigDecimal protein;

    @NotNull(message = "脂肪不能为空")
    private BigDecimal fat;

    private BigDecimal saturatedFat;

    @NotNull(message = "碳水不能为空")
    private BigDecimal carbs;

    private String fiber;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private String imageUrl;
}
