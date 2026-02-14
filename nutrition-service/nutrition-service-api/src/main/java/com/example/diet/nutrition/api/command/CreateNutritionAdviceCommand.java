package com.example.diet.nutrition.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建营养建议命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNutritionAdviceCommand implements Command {

    @NotBlank(message = "条件类型不能为空")
    private String conditionType;

    @NotBlank(message = "建议类型不能为空")
    private String type;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "详情不能为空")
    private String description;

    private Integer minPercentage;

    private Integer maxPercentage;

    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    private Integer priority = 10;

    @Builder.Default
    private Integer status = 1;
}
