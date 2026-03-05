package com.example.diet.gateway.interfaces.rest;

import com.example.diet.dashboard.api.DashboardApi;
import com.example.diet.dashboard.api.query.*;
import com.example.diet.dashboard.api.response.*;
import com.example.diet.record.api.response.DietRecordResponse;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.shared.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 仪表盘控制器 (管理员专用)
 * 代理 dashboard-service 的 Dubbo API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @DubboReference
    private DashboardApi dashboardApi;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        GetDashboardStatsQuery query = GetDashboardStatsQuery.builder()
                .date(date)
                .build();

        DashboardStatsResponse stats = dashboardApi.getStats(query);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取营养趋势数据
     */
    @GetMapping("/nutrition-trend")
    public ResponseEntity<ApiResponse<NutritionTrendResponse>> getNutritionTrend(
            @RequestParam(defaultValue = "week") String period) {

        GetNutritionTrendQuery query = GetNutritionTrendQuery.builder()
                .period(period)
                .build();

        NutritionTrendResponse trend = dashboardApi.getNutritionTrend(query);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    /**
     * 获取最新饮食记录
     */
    @GetMapping("/latest-diet-records")
    public ResponseEntity<ApiResponse<PageResponse<DietRecordResponse>>> getLatestRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        ListLatestRecordsQuery query = ListLatestRecordsQuery.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .build();

        PageResponse<DietRecordResponse> records = dashboardApi.getLatestRecords(query);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 获取热门食物统计
     */
    @GetMapping("/popular-foods")
    public ResponseEntity<ApiResponse<List<PopularFoodResponse>>> getPopularFoods(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(defaultValue = "10") Integer limit) {

        GetPopularFoodsQuery query = GetPopularFoodsQuery.builder()
                .period(period)
                .limit(limit)
                .build();

        List<PopularFoodResponse> foods = dashboardApi.getPopularFoods(query);
        return ResponseEntity.ok(ApiResponse.success(foods));
    }
}
