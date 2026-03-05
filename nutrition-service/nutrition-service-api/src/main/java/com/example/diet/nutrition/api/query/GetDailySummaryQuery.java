package com.example.diet.nutrition.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Daily nutrition summary query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDailySummaryQuery implements Query {
    private Long userId;
    private LocalDate date;
}
