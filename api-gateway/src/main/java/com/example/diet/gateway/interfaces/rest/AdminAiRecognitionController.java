package com.example.diet.gateway.interfaces.rest;

import com.example.diet.shared.response.ApiResponse;
import com.example.diet.user.api.ai.AiRecognitionApi;
import com.example.diet.user.api.ai.query.GetAiRecognitionTopFoodsQuery;
import com.example.diet.user.api.ai.query.GetAiRecognitionTrendQuery;
import com.example.diet.user.api.ai.query.ListAiRecognitionTasksQuery;
import com.example.diet.user.api.ai.response.AiRecognitionStatsResponse;
import com.example.diet.user.api.ai.response.AiRecognitionStatusDistributionResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTaskPageResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTopFoodResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTrendResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理端 AI 识别分析控制器
 */
@RestController
@RequestMapping("/api/admin/ai/recognition")
public class AdminAiRecognitionController {

    @DubboReference
    private AiRecognitionApi aiRecognitionApi;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AiRecognitionStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(aiRecognitionApi.getStats()));
    }

    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<AiRecognitionTrendResponse>> getTrend(
            @RequestParam(defaultValue = "month") String period) {
        GetAiRecognitionTrendQuery query = GetAiRecognitionTrendQuery.builder()
                .period(period)
                .build();
        return ResponseEntity.ok(ApiResponse.success(aiRecognitionApi.getTrend(query)));
    }

    @GetMapping("/status-distribution")
    public ResponseEntity<ApiResponse<List<AiRecognitionStatusDistributionResponse>>> getStatusDistribution() {
        return ResponseEntity.ok(ApiResponse.success(aiRecognitionApi.getStatusDistribution()));
    }

    @GetMapping("/top-foods")
    public ResponseEntity<ApiResponse<List<AiRecognitionTopFoodResponse>>> getTopFoods(
            @RequestParam(defaultValue = "10") Integer limit) {
        GetAiRecognitionTopFoodsQuery query = GetAiRecognitionTopFoodsQuery.builder()
                .limit(limit)
                .build();
        return ResponseEntity.ok(ApiResponse.success(aiRecognitionApi.getTopFoods(query)));
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<AiRecognitionTaskPageResponse>> listTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ListAiRecognitionTasksQuery query = ListAiRecognitionTasksQuery.builder()
                .page(page)
                .pageSize(pageSize)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return ResponseEntity.ok(ApiResponse.success(aiRecognitionApi.listTasks(query)));
    }
}
