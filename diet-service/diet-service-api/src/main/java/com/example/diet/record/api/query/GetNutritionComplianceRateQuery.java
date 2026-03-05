package com.example.diet.record.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 获取营养达标率查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNutritionComplianceRateQuery implements Query {

    private LocalDate date;
}
