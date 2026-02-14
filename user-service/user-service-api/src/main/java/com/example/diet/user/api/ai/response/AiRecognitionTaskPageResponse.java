package com.example.diet.user.api.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AI 识别任务分页响应（按前端契约返回 list）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecognitionTaskPageResponse implements Serializable {

    private List<AiRecognitionTaskResponse> list;

    private Long total;

    private Integer page;

    private Integer pageSize;
}
