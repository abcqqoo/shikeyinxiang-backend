package com.example.diet.record.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 分页查询饮食记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListDietRecordsQuery implements Query {

    private Long userId;

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 餐次类型筛选
     */
    private String mealType;
}
