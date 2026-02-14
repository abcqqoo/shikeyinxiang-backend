package com.example.diet.food.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除食物命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFoodCommand implements Command {

    @NotNull(message = "食物ID不能为空")
    private Long foodId;
}
