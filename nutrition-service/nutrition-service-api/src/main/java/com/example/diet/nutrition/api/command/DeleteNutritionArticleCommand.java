package com.example.diet.nutrition.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除营养文章命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteNutritionArticleCommand implements Command {

    @NotNull(message = "文章ID不能为空")
    private Long id;
}
