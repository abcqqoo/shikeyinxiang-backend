package com.example.diet.file.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成上传URL命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateUploadUrlCommand implements Command {

    /**
     * 文件路径/Key
     */
    @NotBlank(message = "文件路径不能为空")
    private String key;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * URL有效期 (分钟)
     */
    @Builder.Default
    private int expirationMinutes = 15;
}
