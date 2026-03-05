package com.example.diet.gateway.interfaces.rest;

import com.example.diet.gateway.infrastructure.security.SecurityContextUtil;
import com.example.diet.record.api.DietRecordApi;
import com.example.diet.record.api.command.*;
import com.example.diet.record.api.query.*;
import com.example.diet.record.api.response.*;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.shared.response.PageResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 饮食记录控制器
 * 代理 diet-service 的 Dubbo API
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class DietRecordController {

    @DubboReference
    private DietRecordApi dietRecordApi;

    // ==================== 用户操作 ====================

    /**
     * 创建饮食记录
     */
    @PostMapping("/diet-records")
    public ResponseEntity<ApiResponse<DietRecordResponse>> createRecord(
            @Valid @RequestBody CreateDietRecordCommand command) {
        Long userId = getCurrentUserId();
        command = command.toBuilder().userId(userId).build();
        DietRecordResponse record = dietRecordApi.createRecord(command);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    /**
     * 更新饮食记录
     */
    @PutMapping("/diet-records/{id}")
    public ResponseEntity<ApiResponse<DietRecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDietRecordCommand command) {
        Long userId = getCurrentUserId();
        command = command.toBuilder().recordId(id).userId(userId).build();
        DietRecordResponse record = dietRecordApi.updateRecord(command);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    /**
     * 删除饮食记录
     */
    @DeleteMapping("/diet-records/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        dietRecordApi.deleteRecord(DeleteDietRecordCommand.builder()
                .recordId(id)
                .operatorUserId(userId)
                .build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 获取饮食记录详情
     */
    @GetMapping("/diet-records/{id}")
    public ResponseEntity<ApiResponse<DietRecordResponse>> getRecord(@PathVariable Long id) {
        // Note: userId validation should be done in the service layer
        DietRecordResponse record = dietRecordApi.getRecord(
                GetDietRecordQuery.builder().recordId(id).build());
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    /**
     * 分页查询用户饮食记录
     */
    @GetMapping("/diet-records")
    public ResponseEntity<ApiResponse<PageResponse<DietRecordResponse>>> listRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = getCurrentUserId();
        ListDietRecordsQuery query = ListDietRecordsQuery.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        PageResponse<DietRecordResponse> result = dietRecordApi.listRecords(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取用户某日饮食记录
     */
    @GetMapping("/diet-records/by-date")
    public ResponseEntity<ApiResponse<List<DietRecordResponse>>> getRecordsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = getCurrentUserId();
        List<DietRecordResponse> records = dietRecordApi.getRecordsByDate(
                GetRecordsByDateQuery.builder().userId(userId).date(date).build());
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 获取当前用户常用餐模板
     */
    @GetMapping("/diet-records/common-templates")
    public ResponseEntity<ApiResponse<List<CommonMealTemplateResponse>>> listCommonMealTemplates(
            @RequestParam(required = false) String mealType,
            @RequestParam(defaultValue = "6") Integer limit) {

        Long userId = getCurrentUserId();
        List<CommonMealTemplateResponse> templates = dietRecordApi.listCommonMealTemplates(
                ListCommonMealTemplatesQuery.builder()
                        .userId(userId)
                        .mealType(mealType)
                        .limit(limit)
                        .build()
        );
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    // ==================== 管理员操作 ====================

    /**
     * 管理员查询所有用户饮食记录
     */
    @GetMapping("/admin/diet-records")
    public ResponseEntity<ApiResponse<PageResponse<DietRecordResponse>>> adminListRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ListDietRecordsQuery query = ListDietRecordsQuery.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        PageResponse<DietRecordResponse> result = dietRecordApi.listRecords(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 管理员删除饮食记录
     */
    @DeleteMapping("/admin/diet-records/{id}")
    public ResponseEntity<ApiResponse<Void>> adminDeleteRecord(@PathVariable Long id) {
        dietRecordApi.deleteRecord(DeleteDietRecordCommand.builder().recordId(id).build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 管理员获取饮食记录详情
     */
    @GetMapping("/admin/diet-records/{id}")
    public ResponseEntity<ApiResponse<DietRecordResponse>> adminGetRecord(@PathVariable Long id) {
        DietRecordResponse record = dietRecordApi.getRecord(
                GetDietRecordQuery.builder().recordId(id).build());
        return ResponseEntity.ok(ApiResponse.success(record));
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
