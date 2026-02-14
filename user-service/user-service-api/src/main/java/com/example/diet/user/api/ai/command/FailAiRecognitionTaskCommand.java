package com.example.diet.user.api.ai.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标记 AI 识别任务失败命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailAiRecognitionTaskCommand implements Command {

    /**
     * 任务 ID
     */
    private Long taskId;

    /**
     * 模型名称（可为空）
     */
    private String modelName;

    /**
     * 处理耗时（毫秒）
     */
    private Integer processingTimeMs;

    /**
     * 错误信息
     */
    private String errorMessage;
}
