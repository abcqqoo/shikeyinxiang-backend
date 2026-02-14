package com.example.diet.record.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 营养达标率响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionComplianceRateResponse implements Serializable {

    private LocalDate date;
    private Double complianceRate;
    private Integer activeUserCount;
}
