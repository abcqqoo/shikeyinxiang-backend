package com.example.diet.food.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 食物分类响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodCategoryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String color;
    private Integer sortOrder;
    private Long foodCount;
}
