package com.example.diet.gateway.interfaces.rest;

import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.feedback.FeedbackApi;
import com.example.diet.user.api.feedback.command.*;
import com.example.diet.user.api.feedback.query.*;
import com.example.diet.user.api.feedback.response.FeedbackResponse;
import com.example.diet.gateway.infrastructure.security.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户反馈控制器
 * 提供用户提交反馈、查看反馈等功能
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class FeedbackController {

    @DubboReference
    private FeedbackApi feedbackApi;

    // ==================== 用户端接口 ====================

    /**
     * 提交反馈
     */
    @PostMapping("/feedbacks")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createFeedback(
            @Valid @RequestBody CreateFeedbackCommand command) {

        Long userId = getCurrentUserId();
        command.setUserId(userId);

        FeedbackResponse response = feedbackApi.createFeedback(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取我的反馈列表
     */
    @GetMapping("/feedbacks")
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> listMyFeedbacks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Long userId = getCurrentUserId();

        ListUserFeedbacksQuery query = ListUserFeedbacksQuery.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .build();

        PageResponse<FeedbackResponse> result = feedbackApi.listUserFeedbacks(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取我的反馈详情
     */
    @GetMapping("/feedbacks/{id}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedbackDetail(
            @PathVariable Long id) {

        Long userId = getCurrentUserId();

        GetFeedbackQuery query = GetFeedbackQuery.builder()
                .id(id)
                .userId(userId) // 用于权限验证
                .build();

        FeedbackResponse response = feedbackApi.getFeedback(query);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 管理员端接口 ====================

    /**
     * 获取所有反馈列表 (管理员)
     */
    @GetMapping("/admin/feedbacks")
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> listAllFeedbacks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        ListAllFeedbacksQuery query = ListAllFeedbacksQuery.builder()
                .page(page)
                .size(size)
                .status(status)
                .keyword(keyword)
                .build();

        PageResponse<FeedbackResponse> result = feedbackApi.listAllFeedbacks(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 更新反馈状态/回复 (管理员)
     */
    @PutMapping("/admin/feedbacks/{id}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> updateFeedback(
            @PathVariable Long id,
            @RequestBody UpdateFeedbackStatusCommand command) {

        command.setId(id);
        FeedbackResponse response = feedbackApi.updateFeedbackStatus(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除反馈 (管理员)
     */
    @DeleteMapping("/admin/feedbacks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long id) {
        feedbackApi.deleteFeedback(DeleteFeedbackCommand.builder().id(id).build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==================== 私有方法 ====================

    private Long getCurrentUserId() {
        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return userId;
    }
}
