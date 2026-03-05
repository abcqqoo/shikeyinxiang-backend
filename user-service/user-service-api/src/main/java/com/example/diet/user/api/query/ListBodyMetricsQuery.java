package com.example.diet.user.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 查询体重/腰围趋势.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListBodyMetricsQuery implements Query {

    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
}
