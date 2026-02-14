package com.example.diet.food.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除食物分类命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFoodCategoryCommand implements Command {

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}
