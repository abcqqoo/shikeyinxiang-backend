package com.example.diet.user.api.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AI 识别趋势响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecognitionTrendResponse implements Serializable {

    /**
     * 日期列表（M/d）
     */
    private List<String> dateList;

    /**
     * 总识别次数列表
     */
    private List<Long> totalList;

    /**
     * 实际录入饮食记录次数列表
     */
    private List<Long> recordedList;
}
