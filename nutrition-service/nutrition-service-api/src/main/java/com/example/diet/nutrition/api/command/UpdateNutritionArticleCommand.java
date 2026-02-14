package com.example.diet.nutrition.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 更新营养文章命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNutritionArticleCommand implements Command {

    @NotNull(message = "文章ID不能为空")
    private Long id;

    private String title;

    private String cover;

    private String summary;

    private String content;

    private Integer status;

    private LocalDateTime publishAt;

    private Integer sortOrder;
}
