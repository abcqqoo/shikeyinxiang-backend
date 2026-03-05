package com.example.diet.record.infrastructure.persistence.dto;

import lombok.Data;

/**
 * 热门食物统计 DTO
 */
@Data
public class PopularFoodStatDTO {
    private Long foodId;
    private String name;
    private Long count;
}
