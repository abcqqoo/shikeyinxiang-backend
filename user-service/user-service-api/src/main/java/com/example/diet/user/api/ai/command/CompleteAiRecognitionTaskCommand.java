package com.example.diet.user.api.ai.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 标记 AI 识别任务完成命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteAiRecognitionTaskCommand implements Command {

    /**
     * 任务 ID
     */
    private Long taskId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 识别项数量
     */
    private Integer totalItems;

    /**
     * 处理耗时（毫秒）
     */
    private Integer processingTimeMs;

    /**
     * 识别结果项
     */
    private List<AiRecognitionItemCommand> items;
}
