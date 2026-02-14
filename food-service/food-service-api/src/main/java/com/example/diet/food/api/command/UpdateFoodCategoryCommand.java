package com.example.diet.food.api.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新食物分类命令
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoodCategoryCommand implements Command {

    private Long categoryId;

    private String name;
    private String description;
    private String color;
    private Integer sortOrder;
}
