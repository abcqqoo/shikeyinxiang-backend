package com.example.diet.nutrition.api.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除营养建议命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteNutritionAdviceCommand implements Command {
    private Long id;
}
