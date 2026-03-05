package com.example.diet.nutrition.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 营养建议响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionAdviceResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String conditionType;

    private String type;
    private String title;
    private String description;
    private String content;
    private Integer minPercentage;
    private Integer maxPercentage;
    private Boolean isDefault;
    private Integer priority;
    private Integer status;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
