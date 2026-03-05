package com.example.diet.user.api.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 识别热门食物统计项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecognitionTopFoodResponse implements Serializable {

    /**
     * 食物名称
     */
    private String name;

    /**
     * 识别次数
     */
    private Long count;
}
