package com.example.diet.food.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量导入食物命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportFoodsCommand implements Command {

    @NotEmpty(message = "导入数据不能为空")
    private List<CreateFoodCommand> foods;
}
