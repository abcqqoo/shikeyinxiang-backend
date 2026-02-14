package com.example.diet.nutrition.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建营养文章命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNutritionArticleCommand implements Command {

    @NotBlank(message = "文章标题不能为空")
    private String title;

    private String cover;

    private String summary;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Builder.Default
    private Integer status = 0;

    private LocalDateTime publishAt;

    @Builder.Default
    private Integer sortOrder = 0;
}
