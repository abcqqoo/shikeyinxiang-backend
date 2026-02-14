package com.example.diet.user.api.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 识别任务响应项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecognitionTaskResponse implements Serializable {

    private Long id;

    private String username;

    /**
     * completed/failed/pending/cancelled
     */
    private String status;

    private Integer itemsCount;

    /**
     * 秒
     */
    private Double processingTime;

    private List<String> recognizedFoods;

    private LocalDateTime createdAt;
}
