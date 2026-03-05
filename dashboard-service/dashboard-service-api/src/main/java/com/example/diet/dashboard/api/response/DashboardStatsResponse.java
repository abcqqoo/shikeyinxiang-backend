package com.example.diet.dashboard.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 仪表盘统计数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse implements Serializable {

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 指定日期的饮食记录数
     */
    private Integer todayRecords;

    /**
     * 营养达标率 (百分比)
     */
    private Double nutritionComplianceRate;

    /**
     * 推荐采纳率 (百分比)
     */
    private Double recommendationAdoptionRate;

    /**
     * 统计日期
     */
    private LocalDate statisticsDate;
}
