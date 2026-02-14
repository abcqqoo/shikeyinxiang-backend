package com.example.diet.user.infrastructure.dubbo;

import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.UserApi;
import com.example.diet.user.api.command.*;
import com.example.diet.user.api.query.*;
import com.example.diet.user.api.response.*;
import com.example.diet.user.application.UserApplicationService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 用户服务 Dubbo 实现
 * 只负责接收请求并委托给应用服务
 */
@DubboService
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final UserApplicationService userApplicationService;

    // ==================== 命令操作 ====================

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        return userApplicationService.createUser(command);
    }

    @Override
    public UserResponse updateUser(UpdateUserCommand command) {
        return userApplicationService.updateUser(command);
    }

    @Override
    public void changePassword(ChangePasswordCommand command) {
        userApplicationService.changePassword(command);
    }

    @Override
    public AvatarUrlResponse updateAvatar(UpdateAvatarCommand command) {
        return userApplicationService.updateAvatar(command);
    }

    @Override
    public void updateStatus(UpdateUserStatusCommand command) {
        userApplicationService.updateStatus(command);
    }

    @Override
    public void updateNutritionGoal(UpdateNutritionGoalCommand command) {
        userApplicationService.updateNutritionGoal(command);
    }

    @Override
    public BodyMetricResponse upsertBodyMetric(UpsertBodyMetricCommand command) {
        return userApplicationService.upsertBodyMetric(command);
    }

    @Override
    public UserSettingsResponse updateUserSettings(UpdateUserSettingsCommand command) {
        return userApplicationService.updateUserSettings(command);
    }

    // ==================== 查询操作 ====================

    @Override
    public UserResponse getUser(GetUserQuery query) {
        return userApplicationService.getUser(query);
    }

    @Override
    public UserResponse getUserByUsername(GetUserByUsernameQuery query) {
        return userApplicationService.getUserByUsername(query);
    }

    @Override
    public UserResponse getUserByEmail(GetUserByEmailQuery query) {
        return userApplicationService.getUserByEmail(query);
    }

    @Override
    public UserResponse getUserByOpenid(GetUserByOpenidQuery query) {
        return userApplicationService.getUserByOpenid(query);
    }

    @Override
    public PageResponse<UserResponse> listUsers(ListUsersQuery query) {
        return userApplicationService.listUsers(query);
    }

    @Override
    public boolean verifyPassword(VerifyPasswordQuery query) {
        return userApplicationService.verifyPassword(query);
    }

    @Override
    public long countUsers() {
        return userApplicationService.countUsers();
    }

    @Override
    public NutritionGoalResponse getNutritionGoal(GetNutritionGoalQuery query) {
        return userApplicationService.getNutritionGoal(query);
    }

    @Override
    public AvatarUrlResponse generateAvatarUploadUrl(GenerateAvatarUploadUrlQuery query) {
        return userApplicationService.generateAvatarUploadUrl(query);
    }

    @Override
    public AvatarUrlResponse generateAvatarDownloadUrl(GetUserQuery query) {
        return userApplicationService.generateAvatarDownloadUrl(query);
    }

    @Override
    public List<BodyMetricResponse> listBodyMetrics(ListBodyMetricsQuery query) {
        return userApplicationService.listBodyMetrics(query);
    }

    @Override
    public UserSettingsResponse getUserSettings(GetUserSettingsQuery query) {
        return userApplicationService.getUserSettings(query);
    }

}
