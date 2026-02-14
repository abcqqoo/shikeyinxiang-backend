package com.example.diet.user.api.ai.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建 AI 识别任务命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAiRecognitionTaskCommand implements Command {

    /**
     * 发起识别的用户 ID
     */
    private Long userId;

    /**
     * 原始图片 URL（可为空）
     */
    private String imageUrl;

    /**
     * 状态，默认 pending
     */
    @Builder.Default
    private String status = "pending";

    /**
     * 模型名称（可为空）
     */
    private String modelName;
}
