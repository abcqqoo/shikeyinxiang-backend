package com.example.diet.gateway.interfaces.rest;

import com.example.diet.gateway.infrastructure.security.SecurityContextUtil;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.UserApi;
import com.example.diet.user.api.command.*;
import com.example.diet.user.api.query.*;
import com.example.diet.user.api.response.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户控制器
 * 代理到 user-service 的 Dubbo 服务
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    @DubboReference
    private UserApi userApi;

    // ==================== 用户端接口 ====================

    /**
     * 获取当前用户信息
     */
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        Long userId = requireUserId();
        UserResponse user = userApi.getUser(GetUserQuery.builder().userId(userId).build());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody UpdateUserCommand command) {
        Long userId = requireUserId();
        // 确保只能更新自己的信息
        command.setUserId(userId);
        UserResponse user = userApi.updateUser(command);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 获取当前用户营养目标
     */
    @GetMapping("/users/me/nutrition-goal")
    public ResponseEntity<ApiResponse<NutritionGoalResponse>> getNutritionGoal() {
        Long userId = requireUserId();
        NutritionGoalResponse goal = userApi.getNutritionGoal(
                GetNutritionGoalQuery.builder().userId(userId).build()
        );
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    /**
     * 更新当前用户营养目标
     */
    @PutMapping("/users/me/nutrition-goal")
    public ResponseEntity<ApiResponse<Void>> updateNutritionGoal(@RequestBody UpdateNutritionGoalCommand command) {
        Long userId = requireUserId();
        command.setUserId(userId);
        userApi.updateNutritionGoal(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 生成头像上传 URL
     */
    @PostMapping("/users/me/avatar/upload-url")
    public ResponseEntity<ApiResponse<AvatarUrlResponse>> generateAvatarUploadUrl(
            @RequestParam(required = false) String contentType) {
        Long userId = requireUserId();
        AvatarUrlResponse url = userApi.generateAvatarUploadUrl(
                GenerateAvatarUploadUrlQuery.builder()
                        .userId(userId)
                        .contentType(contentType)
                        .build()
        );
        return ResponseEntity.ok(ApiResponse.success(url));
    }

    /**
     * 更新头像 (上传完成后调用)
     */
    @PutMapping("/users/me/avatar")
    public ResponseEntity<ApiResponse<AvatarUrlResponse>> updateAvatar(@Valid @RequestBody UpdateAvatarCommand command) {
        Long userId = requireUserId();
        command.setUserId(userId);
        AvatarUrlResponse url = userApi.updateAvatar(command);
        return ResponseEntity.ok(ApiResponse.success(url));
    }

    /**
     * 新增或更新当前用户体重/腰围记录
     */
    @PutMapping("/users/me/body-metrics")
    public ResponseEntity<ApiResponse<BodyMetricResponse>> upsertBodyMetric(
            @RequestBody UpsertBodyMetricCommand command) {
        Long userId = requireUserId();
        command.setUserId(userId);
        BodyMetricResponse response = userApi.upsertBodyMetric(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取当前用户体重/腰围趋势
     */
    @GetMapping("/users/me/body-metrics/trend")
    public ResponseEntity<ApiResponse<List<BodyMetricResponse>>> listBodyMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = requireUserId();
        List<BodyMetricResponse> responses = userApi.listBodyMetrics(
                ListBodyMetricsQuery.builder()
                        .userId(userId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .build()
        );
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 获取当前用户设置
     */
    @GetMapping("/users/me/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getUserSettings() {
        Long userId = requireUserId();
        UserSettingsResponse response = userApi.getUserSettings(
                GetUserSettingsQuery.builder().userId(userId).build()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新当前用户设置
     */
    @PutMapping("/users/me/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> updateUserSettings(
            @RequestBody UpdateUserSettingsCommand command) {
        Long userId = requireUserId();
        command.setUserId(userId);
        UserSettingsResponse response = userApi.updateUserSettings(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 管理端接口 ====================

    /**
     * 分页查询用户列表 (管理员)
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {

        ListUsersQuery query = ListUsersQuery.builder()
                .page(page)
                .size(size)
                .keyword(keyword)
                .role(role)
                .status(status)
                .build();

        PageResponse<UserResponse> result = userApi.listUsers(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取用户详情 (管理员)
     */
    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        UserResponse user = userApi.getUser(GetUserQuery.builder().userId(userId).build());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 创建用户 (管理员)
     */
    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserCommand command) {
        UserResponse user = userApi.createUser(command);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户 (管理员)
     */
    @PutMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserCommand command) {
        command.setUserId(userId);
        UserResponse user = userApi.updateUser(command);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户状态 (管理员)
     */
    @PatchMapping("/admin/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusCommand command) {
        command.setUserId(userId);
        userApi.updateStatus(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 获取用户总数 (管理员)
     */
    @GetMapping("/admin/users/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countUsers() {
        long count = userApi.countUsers();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前用户 ID (必须已认证)
     */
    private Long requireUserId() {
        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未授权");
        }
        return userId;
    }
}
