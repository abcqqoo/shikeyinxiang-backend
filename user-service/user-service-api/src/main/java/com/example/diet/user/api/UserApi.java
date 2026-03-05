package com.example.diet.user.api;

import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.command.*;
import com.example.diet.user.api.query.*;
import com.example.diet.user.api.response.*;

import java.util.List;

/**
 * 用户服务 Dubbo API
 * 提供用户管理、认证、营养目标等功能
 */
public interface UserApi {

    // ==================== 命令操作 (写) ====================

    /**
     * 创建用户
     */
    UserResponse createUser(CreateUserCommand command);

    /**
     * 更新用户信息
     */
    UserResponse updateUser(UpdateUserCommand command);

    /**
     * 修改密码
     */
    void changePassword(ChangePasswordCommand command);

    /**
     * 更新头像
     */
    AvatarUrlResponse updateAvatar(UpdateAvatarCommand command);

    /**
     * 更新用户状态 (启用/禁用)
     */
    void updateStatus(UpdateUserStatusCommand command);

    /**
     * 更新营养目标
     */
    void updateNutritionGoal(UpdateNutritionGoalCommand command);

    /**
     * 新增或更新体重/腰围记录
     */
    BodyMetricResponse upsertBodyMetric(UpsertBodyMetricCommand command);

    /**
     * 更新用户设置
     */
    UserSettingsResponse updateUserSettings(UpdateUserSettingsCommand command);

    // ==================== 查询操作 (读) ====================

    /**
     * 根据 ID 获取用户
     */
    UserResponse getUser(GetUserQuery query);

    /**
     * 根据用户名获取用户
     */
    UserResponse getUserByUsername(GetUserByUsernameQuery query);

    /**
     * 根据邮箱获取用户
     */
    UserResponse getUserByEmail(GetUserByEmailQuery query);

    /**
     * 根据微信 OpenID 获取用户
     */
    UserResponse getUserByOpenid(GetUserByOpenidQuery query);

    /**
     * 分页查询用户列表
     */
    PageResponse<UserResponse> listUsers(ListUsersQuery query);

    /**
     * 验证密码
     */
    boolean verifyPassword(VerifyPasswordQuery query);

    /**
     * 获取用户总数
     */
    long countUsers();

    /**
     * 获取用户营养目标
     */
    NutritionGoalResponse getNutritionGoal(GetNutritionGoalQuery query);

    /**
     * 生成头像上传 URL
     */
    AvatarUrlResponse generateAvatarUploadUrl(GenerateAvatarUploadUrlQuery query);

    /**
     * 生成头像下载 URL
     */
    AvatarUrlResponse generateAvatarDownloadUrl(GetUserQuery query);

    /**
     * 查询体重/腰围趋势
     */
    List<BodyMetricResponse> listBodyMetrics(ListBodyMetricsQuery query);

    /**
     * 获取用户设置
     */
    UserSettingsResponse getUserSettings(GetUserSettingsQuery query);

}
