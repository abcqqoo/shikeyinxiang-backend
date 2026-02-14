package com.example.diet.food.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建食物分类命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFoodCategoryCommand implements Command {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    private String color;

    private Integer sortOrder;
}
