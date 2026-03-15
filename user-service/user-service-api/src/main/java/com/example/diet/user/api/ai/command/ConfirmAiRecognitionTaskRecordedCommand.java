package com.example.diet.user.api.ai.command;

import com.example.diet.shared.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 确认 AI 识别结果已录入饮食记录命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmAiRecognitionTaskRecordedCommand implements Command {

    private Long taskId;

    private Long userId;

    private List<String> selectedFoodNames;
}
