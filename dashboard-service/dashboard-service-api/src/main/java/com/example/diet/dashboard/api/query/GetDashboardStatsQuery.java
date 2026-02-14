package com.example.diet.dashboard.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 获取仪表盘统计数据查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDashboardStatsQuery implements Query {

    /**
     * 统计日期，默认今天
     */
    private LocalDate date;
}
