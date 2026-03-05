package com.example.diet.user.api.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 识别统计概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecognitionStatsResponse implements Serializable {

    /**
     * 今日识别总数
     */
    private Long todayTotal;

    /**
     * 识别成功率（百分比）
     */
    private Double successRate;

    /**
     * 平均处理时长（秒）
     */
    private Double avgProcessingTime;

    /**
     * 今日最常识别食物
     */
    private String topFood;
}
