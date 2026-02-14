package com.example.diet.user.api.ai.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI 识别结果项命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecognitionItemCommand implements Serializable {

    private String foodName;

    private BigDecimal confidence;

    private BigDecimal calories;

    private BigDecimal proteinG;

    private BigDecimal fatG;

    private BigDecimal carbsG;

    private Integer estimatedGrams;

    private Boolean wasSelected;
}
