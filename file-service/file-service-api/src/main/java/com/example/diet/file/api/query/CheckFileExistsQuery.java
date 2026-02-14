package com.example.diet.file.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检查文件是否存在查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckFileExistsQuery implements Query {

    @NotBlank(message = "文件路径不能为空")
    private String key;
}
