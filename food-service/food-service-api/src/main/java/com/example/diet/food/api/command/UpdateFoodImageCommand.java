package com.example.diet.food.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新食物图片命令
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoodImageCommand implements Command {

    @NotNull(message = "食物ID不能为空")
    private Long foodId;

    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;
}
