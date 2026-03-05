package com.example.diet.file.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成下载URL查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDownloadUrlQuery implements Query {

    @NotBlank(message = "文件路径不能为空")
    private String key;

    /**
     * URL有效期 (分钟)
     */
    @Builder.Default
    private int expirationMinutes = 60;
}
