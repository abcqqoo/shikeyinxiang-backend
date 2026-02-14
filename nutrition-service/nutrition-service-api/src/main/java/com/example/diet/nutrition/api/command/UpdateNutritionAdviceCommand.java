package com.example.diet.nutrition.api.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新营养建议命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNutritionAdviceCommand implements Command {

    private Long id;

    private String conditionType;

    private String type;

    private String title;

    private String description;

    private Integer minPercentage;

    private Integer maxPercentage;

    private Boolean isDefault;

    private Integer priority;

    private Integer status;
}
