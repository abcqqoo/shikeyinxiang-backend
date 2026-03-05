package com.example.diet.user.api.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 识别状态分布项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecognitionStatusDistributionResponse implements Serializable {

    /**
     * 状态名称（中文）
     */
    private String name;

    /**
     * 数量
     */
    private Long value;

    /**
     * ECharts 样式
     */
    private Map<String, String> itemStyle;
}
