package com.example.diet.record.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 热门食物统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularFoodStatResponse implements Serializable {

    private Long foodId;
    private String name;
    private Long count;
    private Boolean isRecipe;
}
