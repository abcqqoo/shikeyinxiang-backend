package com.example.diet.gateway.interfaces.rest;

import com.example.diet.gateway.infrastructure.security.SecurityContextUtil;
import com.example.diet.nutrition.api.NutritionApi;
import com.example.diet.nutrition.api.query.*;
import com.example.diet.nutrition.api.response.*;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养分析控制器
 * 代理 nutrition-service 的 Dubbo API
 */
@Slf4j
@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    @DubboReference
    private NutritionApi nutritionApi;

    /**
     * 获取每日营养汇总
     */
    @GetMapping("/daily-summary")
    public ResponseEntity<ApiResponse<DailyNutritionSummaryResponse>> getDailySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = getCurrentUserId();
        DailyNutritionSummaryResponse summary = nutritionApi.getDailySummary(
                GetDailySummaryQuery.builder()
                        .userId(userId)
                        .date(date != null ? date : LocalDate.now())
                        .build());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * 获取营养趋势
     */
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<NutritionTrendResponse>> getNutritionTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type) {

        Long userId = getCurrentUserId();
        NutritionTrendResponse trend = nutritionApi.getNutritionTrend(
                GetNutritionTrendQuery.builder()
                        .userId(userId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .type(type)
                        .build());
        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    /**
     * 获取营养建议
     */
    @GetMapping("/advice")
    public ResponseEntity<ApiResponse<List<NutritionAdviceResponse>>> getAdvice(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = getCurrentUserId();
        List<NutritionAdviceResponse> advice = nutritionApi.getAdvice(
                GetNutritionAdviceQuery.builder()
                        .userId(userId)
                        .date(date)
                        .build());
        return ResponseEntity.ok(ApiResponse.success(advice));
    }

    /**
     * 获取健康报告
     */
    @GetMapping("/health-report")
    public ResponseEntity<ApiResponse<HealthReportResponse>> getHealthReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {

        Long userId = getCurrentUserId();
        HealthReportResponse report = nutritionApi.getHealthReport(
                GetHealthReportQuery.builder()
                        .userId(userId)
                        .weekStart(weekStart)
                        .build());
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // ==================== 辅助方法 ====================

    private Long getCurrentUserId() {
        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未授权");
        }
        return userId;
    }
}
