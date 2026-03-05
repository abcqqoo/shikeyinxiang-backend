package com.example.diet.user.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增或更新体重/腰围记录.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertBodyMetricCommand implements Command {

    private Long userId;

    /**
     * 记录日期，默认当天。
     */
    private LocalDate recordDate;

    /**
     * 体重（kg）。
     */
    @Positive(message = "体重必须大于0")
    private BigDecimal weightKg;

    /**
     * 腰围（cm）。
     */
    @Positive(message = "腰围必须大于0")
    private BigDecimal waistCm;
}
