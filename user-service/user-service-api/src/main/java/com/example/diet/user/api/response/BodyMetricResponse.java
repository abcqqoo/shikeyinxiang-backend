package com.example.diet.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体重/腰围记录响应.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyMetricResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private BigDecimal weightKg;
    private BigDecimal waistCm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
