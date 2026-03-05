package com.example.diet.record.infrastructure.persistence.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 每日活跃用户统计 DTO
 */
@Data
public class DailyUserCountDTO {
    private LocalDate recordDate;
    private Long userCount;
}
