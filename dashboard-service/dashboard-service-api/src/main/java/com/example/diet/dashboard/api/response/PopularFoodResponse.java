package com.example.diet.dashboard.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 热门食物响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularFoodResponse implements Serializable {

    /**
     * 食物ID
     */
    private Long foodId;

    /**
     * 名称(普通食物或推荐菜肴)
     */
    private String name;

    /**
     * 记录次数
     */
    private Long count;

    /**
     * 是否为推荐菜肴
     */
    private Boolean isRecipe;
}
