package com.example.diet.dashboard.api;

import com.example.diet.dashboard.api.query.*;
import com.example.diet.dashboard.api.response.*;
import com.example.diet.record.api.response.DietRecordResponse;
import com.example.diet.shared.response.PageResponse;

import java.util.List;

/**
 * 仪表盘服务 Dubbo API
 * 用于管理员后台统计数据展示
 */
public interface DashboardApi {

    /**
     * 获取仪表盘统计数据
     * 包括：总用户数、今日饮食记录数、营养达标率等
     */
    DashboardStatsResponse getStats(GetDashboardStatsQuery query);

    /**
     * 获取营养摄入趋势
     * 支持周/月/年维度
     */
    NutritionTrendResponse getNutritionTrend(GetNutritionTrendQuery query);

    /**
     * 获取最新饮食记录列表（所有用户）
     */
    PageResponse<DietRecordResponse> getLatestRecords(ListLatestRecordsQuery query);

    /**
     * 获取热门食物统计
     */
    List<PopularFoodResponse> getPopularFoods(GetPopularFoodsQuery query);
}
