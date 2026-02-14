package com.example.diet.file.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除文件命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFileCommand implements Command {

    @NotBlank(message = "文件路径不能为空")
    private String key;
}
