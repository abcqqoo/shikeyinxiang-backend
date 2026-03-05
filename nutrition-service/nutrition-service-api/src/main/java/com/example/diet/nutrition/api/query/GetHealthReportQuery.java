package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Health report query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetHealthReportQuery implements Query {
    private Long userId;
    /**
     * 周开始日期（周一）
     */
    private LocalDate weekStart;
}
