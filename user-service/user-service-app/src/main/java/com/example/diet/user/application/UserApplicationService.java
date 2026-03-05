package com.example.diet.user.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.diet.file.api.FileApi;
import com.example.diet.file.api.command.DeleteFileCommand;
import com.example.diet.file.api.command.GenerateUploadUrlCommand;
import com.example.diet.file.api.query.GenerateDownloadUrlQuery;
import com.example.diet.file.api.response.PresignedUrlResponse;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;
import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.command.*;
import com.example.diet.user.api.query.*;
import com.example.diet.user.api.response.AvatarUrlResponse;
import com.example.diet.user.api.response.BodyMetricResponse;
import com.example.diet.user.api.response.NutritionGoalResponse;
import com.example.diet.user.api.response.UserResponse;
import com.example.diet.user.api.response.UserSettingsResponse;
import com.example.diet.user.application.assembler.UserAssembler;
import com.example.diet.user.domain.model.*;
import com.example.diet.user.domain.repository.UserRepository;
import com.example.diet.user.infrastructure.persistence.converter.UserConverter;
import com.example.diet.user.infrastructure.persistence.mapper.UserBodyMetricMapper;
import com.example.diet.user.infrastructure.persistence.mapper.UserMapper;
import com.example.diet.user.infrastructure.persistence.mapper.UserSettingsMapper;
import com.example.diet.user.infrastructure.persistence.mapper.UserNutritionGoalMapper;
import com.example.diet.user.infrastructure.persistence.po.UserBodyMetricPO;
import com.example.diet.user.infrastructure.persistence.po.UserSettingsPO;
import com.example.diet.user.infrastructure.persistence.po.UserNutritionGoalPO;
import com.example.diet.user.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户应用服务
 * 负责编排领域对象，处理事务，不包含业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserAssembler userAssembler;
    private final ApplicationEventPublisher eventPublisher;
    private final UserMapper userMapper;
    private final UserConverter userConverter;
    private final UserNutritionGoalMapper userNutritionGoalMapper;
    private final UserBodyMetricMapper userBodyMetricMapper;
    private final UserSettingsMapper userSettingsMapper;

    @DubboReference
    private FileApi fileApi;

    private static final int AVATAR_UPLOAD_EXPIRE_MINUTES = 15;
    private static final int AVATAR_DOWNLOAD_EXPIRE_MINUTES = 60;
    private static final BigDecimal DEFAULT_CALORIE_TARGET = BigDecimal.valueOf(2200);
    private static final BigDecimal DEFAULT_PROTEIN_TARGET = BigDecimal.valueOf(65);
    private static final BigDecimal DEFAULT_CARBS_TARGET = BigDecimal.valueOf(300);
    private static final BigDecimal DEFAULT_FAT_TARGET = BigDecimal.valueOf(70);
    private static final boolean DEFAULT_ALLOW_DATA_ANALYSIS = true;
    private static final boolean DEFAULT_ALLOW_PERSONALIZATION = true;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\u4e00-\\u9fa5]{3,20}$");

    private static final Map<String, SFunction<UserPO, ?>> SORT_FIELDS = Map.of(
            "id", UserPO::getId,
            "username", UserPO::getUsername,
            "email", UserPO::getEmail,
            "status", UserPO::getStatus,
            "role", UserPO::getRole,
            "createTime", UserPO::getCreateTime
    );

    // ==================== 命令操作 ====================

    @Transactional
    public UserResponse createUser(CreateUserCommand command) {
        log.info("创建用户: username={}", command.getUsername());

        // 验证唯一性
        Username username = Username.of(command.getUsername());
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        Email email = null;
        if (command.getEmail() != null && !command.getEmail().isBlank()) {
            email = Email.of(command.getEmail());
            if (userRepository.existsByEmail(email)) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
        }

        // 创建用户
        Password password = null;
        if (command.getPassword() != null && !command.getPassword().isBlank()) {
            password = Password.fromPlainText(command.getPassword());
        }

        User user;
        if (command.getOpenid() != null && !command.getOpenid().isBlank()) {
            // 微信注册
            user = User.createWithOpenid(command.getOpenid(), username);
            if (email != null) {
                user.updateProfile(null, email);
            }
        } else {
            // 普通注册
            if (email == null) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "邮箱不能为空");
            }
            if (password == null) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "密码不能为空");
            }
            user = User.create(username, email, password);
        }

        // 设置可选属性
        if (command.getAvatarUrl() != null) {
            user.updateAvatar(command.getAvatarUrl());
        }

        // 持久化
        userRepository.save(user);

        // 发布领域事件
        publishDomainEvents(user);

        UserResponse response = userAssembler.toResponse(user);
        attachAvatarUrls(List.of(response));
        return response;
    }

    @Transactional
    public UserResponse updateUser(UpdateUserCommand command) {
        Long userId = requireUserId(command.getUserId());
        log.info("更新用户: id={}", userId);

        User user = findUserById(userId);

        // 验证唯一性（如果修改了用户名或邮箱）
        if (command.getUsername() != null) {
            Username newUsername = Username.of(command.getUsername());
            if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS);
            }
        }

        if (command.getEmail() != null) {
            Email newEmail = Email.of(command.getEmail());
            if (user.getEmail() == null || !newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new BusinessException(ErrorCode.EMAIL_EXISTS);
                }
            }
        }

        // 更新用户信息
        user.updateProfile(
                command.getUsername() != null ? Username.of(command.getUsername()) : null,
                command.getEmail() != null ? Email.of(command.getEmail()) : null
        );

        if (command.getAvatarUrl() != null) {
            user.updateAvatar(command.getAvatarUrl());
        }

        userRepository.save(user);
        publishDomainEvents(user);

        UserResponse response = userAssembler.toResponse(user);
        attachAvatarUrls(List.of(response));
        return response;
    }

    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        Long userId = requireUserId(command.getUserId());
        log.info("修改密码: userId={}", userId);

        User user = findUserById(userId);
        Password newPassword = Password.fromPlainText(command.getNewPassword());
        user.changePassword(newPassword);

        userRepository.save(user);
        publishDomainEvents(user);
    }

    @Transactional
    public void updateStatus(UpdateUserStatusCommand command) {
        Long userId = requireUserId(command.getUserId());
        log.info("更新用户状态: userId={}, status={}", userId, command.getStatus());

        User user = findUserById(userId);

        if (command.getStatus() == UserStatus.ACTIVE.getValue()) {
            user.enable();
        } else {
            user.disable();
        }

        userRepository.save(user);
        publishDomainEvents(user);
    }

    @Transactional
    public AvatarUrlResponse updateAvatar(UpdateAvatarCommand command) {
        Long userId = requireUserId(command.getUserId());
        log.info("更新用户头像: userId={}", userId);

        if (!StringUtils.hasText(command.getAvatarUrl())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "头像Key不能为空");
        }

        User user = findUserById(userId);
        String oldAvatarKey = user.getAvatarUrl();
        String newAvatarKey = command.getAvatarUrl();

        if (!newAvatarKey.equals(oldAvatarKey)) {
            user.updateAvatar(newAvatarKey);
            userRepository.save(user);
            publishDomainEvents(user);
            deleteOldAvatarIfNeeded(oldAvatarKey, newAvatarKey);
        }

        return buildAvatarDownloadResponse(newAvatarKey);
    }

    @Transactional
    public void updateNutritionGoal(UpdateNutritionGoalCommand command) {
        Long userId = requireUserId(command.getUserId());
        command.setUserId(userId);
        log.info("更新营养目标: userId={}", userId);

        UserNutritionGoalPO existing = userNutritionGoalMapper.selectByUserId(userId);
        if (existing == null) {
            UserNutritionGoalPO created = buildNutritionGoalPO(command, true);
            userNutritionGoalMapper.insert(created);
            return;
        }

        LambdaUpdateWrapper<UserNutritionGoalPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserNutritionGoalPO::getUserId, userId);

        boolean hasUpdate = applyNutritionGoalUpdate(updateWrapper, command);
        if (!hasUpdate) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "未提供可更新的营养目标字段");
        }

        updateWrapper.set(UserNutritionGoalPO::getUpdatedAt, LocalDateTime.now());
        int updatedRows = userNutritionGoalMapper.update(null, updateWrapper);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.NUTRITION_GOAL_NOT_FOUND);
        }
    }

    @Transactional
    public BodyMetricResponse upsertBodyMetric(UpsertBodyMetricCommand command) {
        Long userId = requireUserId(command.getUserId());
        if (command.getWeightKg() == null && command.getWaistCm() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "体重和腰围不能同时为空");
        }
        if (command.getWeightKg() != null && command.getWeightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "体重必须大于0");
        }
        if (command.getWaistCm() != null && command.getWaistCm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "腰围必须大于0");
        }

        // 校验用户存在
        findUserById(userId);

        LocalDate recordDate = command.getRecordDate() != null ? command.getRecordDate() : LocalDate.now();
        UserBodyMetricPO existing = userBodyMetricMapper.selectByUserIdAndRecordDate(userId, recordDate);

        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            UserBodyMetricPO created = new UserBodyMetricPO();
            created.setUserId(userId);
            created.setRecordDate(recordDate);
            created.setWeightKg(command.getWeightKg());
            created.setWaistCm(command.getWaistCm());
            created.setCreatedAt(now);
            created.setUpdatedAt(now);
            userBodyMetricMapper.insert(created);
            return toBodyMetricResponse(created);
        }

        if (command.getWeightKg() != null) {
            existing.setWeightKg(command.getWeightKg());
        }
        if (command.getWaistCm() != null) {
            existing.setWaistCm(command.getWaistCm());
        }
        existing.setUpdatedAt(now);
        userBodyMetricMapper.updateById(existing);
        return toBodyMetricResponse(existing);
    }

    @Transactional
    public UserSettingsResponse updateUserSettings(UpdateUserSettingsCommand command) {
        Long userId = requireUserId(command.getUserId());
        findUserById(userId);

        UserSettingsPO existing = userSettingsMapper.selectByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            UserSettingsPO created = buildDefaultSettings(userId, now);
            applySettingsUpdate(created, command);
            userSettingsMapper.insert(created);
            return toUserSettingsResponse(created);
        }

        applySettingsUpdate(existing, command);
        existing.setUpdatedAt(now);
        userSettingsMapper.updateById(existing);
        return toUserSettingsResponse(existing);
    }

    @Transactional
    public AvatarUrlResponse generateAvatarUploadUrl(GenerateAvatarUploadUrlQuery query) {
        Long userId = requireUserId(query.getUserId());
        if (!StringUtils.hasText(query.getContentType())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "文件类型不能为空");
        }

        String extension = resolveExtension(query.getContentType());
        if (extension == null) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, "文件类型不支持");
        }

        String key = String.format("avatar/%s/%s.%s",
                userId,
                UUID.randomUUID().toString().replace("-", ""),
                extension);

        PresignedUrlResponse presigned = fileApi.generateUploadUrl(
                GenerateUploadUrlCommand.builder()
                        .key(key)
                        .contentType(query.getContentType())
                        .expirationMinutes(AVATAR_UPLOAD_EXPIRE_MINUTES)
                        .build()
        );

        return new AvatarUrlResponse(presigned.getUrl(), presigned.getKey());
    }

    // ==================== 查询操作 ====================

    @Transactional(readOnly = true)
    public UserResponse getUser(GetUserQuery query) {
        Long userId = requireUserId(query.getUserId());
        User user = findUserById(userId);
        UserResponse response = userAssembler.toResponse(user);
        attachAvatarUrls(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(GetUserByUsernameQuery query) {
        Username username = Username.of(query.getUsername());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserResponse response = userAssembler.toResponse(user);
        attachAvatarUrls(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(GetUserByEmailQuery query) {
        Email email = Email.of(query.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserResponse response = userAssembler.toResponse(user);
        attachAvatarUrls(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByOpenid(GetUserByOpenidQuery query) {
        User user = userRepository.findByOpenid(query.getOpenid())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserResponse response = userAssembler.toResponse(user);
        attachAvatarUrls(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(VerifyPasswordQuery query) {
        String identifier = query.getIdentifier();
        if (!StringUtils.hasText(identifier) || !StringUtils.hasText(query.getPassword())) {
            return false;
        }

        String trimmedIdentifier = identifier.trim();
        User user;
        if (isEmailIdentifier(trimmedIdentifier)) {
            user = userRepository.findByEmail(Email.of(trimmedIdentifier)).orElse(null);
        } else if (isUsernameIdentifier(trimmedIdentifier)) {
            user = userRepository.findByUsername(Username.of(trimmedIdentifier)).orElse(null);
        } else {
            return false;
        }

        if (user == null) {
            return false;
        }

        return user.verifyPassword(query.getPassword());
    }

    private boolean isEmailIdentifier(String identifier) {
        return EMAIL_PATTERN.matcher(identifier).matches();
    }

    private boolean isUsernameIdentifier(String identifier) {
        return USERNAME_PATTERN.matcher(identifier).matches();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(ListUsersQuery query) {
        int page = Math.max(query.getPage(), 1);
        int size = Math.max(query.getSize(), 1);

        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(UserPO::getUsername, keyword)
                    .or()
                    .like(UserPO::getEmail, keyword));
        }
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(UserPO::getUsername, query.getUsername().trim());
        }
        if (StringUtils.hasText(query.getEmail())) {
            wrapper.like(UserPO::getEmail, query.getEmail().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(UserPO::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getRole())) {
            wrapper.eq(UserPO::getRole, query.getRole().trim());
        }

        applyUserSort(wrapper, query);

        IPage<UserPO> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        if (result.getRecords().isEmpty()) {
            return PageResponse.empty(page, size);
        }

        List<UserResponse> records = result.getRecords().stream()
                .map(userConverter::toDomain)
                .filter(Objects::nonNull)
                .map(userAssembler::toResponse)
                .collect(Collectors.toList());

        attachAvatarUrls(records);

        return PageResponse.of(records, result.getTotal(), page, size);
    }

    @Transactional(readOnly = true)
    public NutritionGoalResponse getNutritionGoal(GetNutritionGoalQuery query) {
        Long userId = requireUserId(query.getUserId());

        UserNutritionGoalPO po = userNutritionGoalMapper.selectByUserId(userId);
        if (po == null) {
            po = createDefaultNutritionGoal(userId);
        }

        return toNutritionGoalResponse(po);
    }

    @Transactional(readOnly = true)
    public AvatarUrlResponse generateAvatarDownloadUrl(GetUserQuery query) {
        Long userId = requireUserId(query.getUserId());
        User user = findUserById(userId);
        return buildAvatarDownloadResponse(user.getAvatarUrl());
    }

    @Transactional(readOnly = true)
    public List<BodyMetricResponse> listBodyMetrics(ListBodyMetricsQuery query) {
        Long userId = requireUserId(query.getUserId());
        // 校验用户存在
        findUserById(userId);

        LocalDate endDate = query.getEndDate() != null ? query.getEndDate() : LocalDate.now();
        LocalDate startDate = query.getStartDate() != null ? query.getStartDate() : endDate.minusDays(55);
        if (startDate.isAfter(endDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }
        if (startDate.isBefore(endDate.minusDays(365))) {
            startDate = endDate.minusDays(365);
        }

        return userBodyMetricMapper.selectByRange(userId, startDate, endDate).stream()
                .map(this::toBodyMetricResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSettingsResponse getUserSettings(GetUserSettingsQuery query) {
        Long userId = requireUserId(query.getUserId());
        findUserById(userId);
        UserSettingsPO settings = getOrCreateSettings(userId);
        return toUserSettingsResponse(settings);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    // ==================== 私有方法 ====================

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户ID不能为空");
        }
        return userId;
    }

    private User findUserById(Long id) {
        return userRepository.findById(UserId.of(id))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void publishDomainEvents(User user) {
        user.getDomainEvents().forEach(eventPublisher::publishEvent);
        user.clearDomainEvents();
    }

    private void applyUserSort(LambdaQueryWrapper<UserPO> wrapper, ListUsersQuery query) {
        String sortBy = StringUtils.hasText(query.getSortBy()) ? query.getSortBy().trim() : "createTime";
        SFunction<UserPO, ?> sortField = SORT_FIELDS.getOrDefault(sortBy, UserPO::getCreateTime);
        boolean asc = "asc".equalsIgnoreCase(query.getSortOrder());
        wrapper.orderBy(true, asc, sortField);
    }

    private void attachAvatarUrls(List<UserResponse> users) {
        for (UserResponse user : users) {
            if (!StringUtils.hasText(user.getAvatarUrl())) {
                continue;
            }
            try {
                PresignedUrlResponse presigned = fileApi.generateDownloadUrl(
                        GenerateDownloadUrlQuery.builder()
                                .key(user.getAvatarUrl())
                                .expirationMinutes(AVATAR_DOWNLOAD_EXPIRE_MINUTES)
                                .build()
                );
                user.setAvatarUrl(presigned.getUrl());
            } catch (BusinessException e) {
                log.warn("action=resolve_user_avatar_url_failed userId={} code={} message={} fallback=empty_avatar",
                        user.getId(), e.getCode(), e.getMessage());
                user.setAvatarUrl("");
            } catch (Exception e) {
                log.error("action=resolve_user_avatar_url_failed userId={} message={} fallback=empty_avatar",
                        user.getId(), e.getMessage(), e);
                user.setAvatarUrl("");
            }
        }
    }

    private AvatarUrlResponse buildAvatarDownloadResponse(String avatarKey) {
        if (!StringUtils.hasText(avatarKey)) {
            return new AvatarUrlResponse("", "");
        }

        try {
            PresignedUrlResponse presigned = fileApi.generateDownloadUrl(
                    GenerateDownloadUrlQuery.builder()
                            .key(avatarKey)
                            .expirationMinutes(AVATAR_DOWNLOAD_EXPIRE_MINUTES)
                            .build()
            );
            return new AvatarUrlResponse(presigned.getUrl(), presigned.getKey());
        } catch (BusinessException e) {
            log.warn("action=generate_avatar_download_url_failed key={} code={} message={} fallback=empty_url",
                    avatarKey, e.getCode(), e.getMessage());
            return new AvatarUrlResponse("", avatarKey);
        } catch (Exception e) {
            log.error("action=generate_avatar_download_url_failed key={} message={} fallback=empty_url",
                    avatarKey, e.getMessage(), e);
            return new AvatarUrlResponse("", avatarKey);
        }
    }

    private void updateAvatarInternal(Long userId, String avatarKey) {
        User user = findUserById(userId);
        String oldAvatarKey = user.getAvatarUrl();

        user.updateAvatar(avatarKey);
        userRepository.save(user);
        publishDomainEvents(user);

        deleteOldAvatarIfNeeded(oldAvatarKey, avatarKey);
    }

    private void deleteOldAvatarIfNeeded(String oldAvatarKey, String newAvatarKey) {
        if (!StringUtils.hasText(oldAvatarKey) || oldAvatarKey.equals(newAvatarKey)) {
            return;
        }

        try {
            fileApi.deleteFile(DeleteFileCommand.builder().key(oldAvatarKey).build());
        } catch (BusinessException e) {
            log.warn("action=delete_old_avatar_failed key={} code={} message={} fallback=ignore",
                    oldAvatarKey, e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("action=delete_old_avatar_failed key={} message={} fallback=ignore",
                    oldAvatarKey, e.getMessage(), e);
        }
    }

    private UserNutritionGoalPO createDefaultNutritionGoal(Long userId) {
        UserNutritionGoalPO po = new UserNutritionGoalPO();
        po.setUserId(userId);
        po.setCalorieTarget(DEFAULT_CALORIE_TARGET);
        po.setProteinTarget(DEFAULT_PROTEIN_TARGET);
        po.setCarbsTarget(DEFAULT_CARBS_TARGET);
        po.setFatTarget(DEFAULT_FAT_TARGET);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        userNutritionGoalMapper.insert(po);
        return po;
    }

    private boolean applyNutritionGoalUpdate(
            LambdaUpdateWrapper<UserNutritionGoalPO> updateWrapper,
            UpdateNutritionGoalCommand command
    ) {
        boolean hasUpdate = false;

        if (command.getCalorieTarget() != null) {
            if (command.getCalorieTarget().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.INVALID_NUTRITION_VALUE, "卡路里目标必须大于0");
            }
            updateWrapper.set(UserNutritionGoalPO::getCalorieTarget, command.getCalorieTarget());
            hasUpdate = true;
        }
        if (command.getProteinTarget() != null) {
            if (command.getProteinTarget().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.INVALID_NUTRITION_VALUE, "蛋白质目标必须大于0");
            }
            updateWrapper.set(UserNutritionGoalPO::getProteinTarget, command.getProteinTarget());
            hasUpdate = true;
        }
        if (command.getCarbsTarget() != null) {
            if (command.getCarbsTarget().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.INVALID_NUTRITION_VALUE, "碳水目标必须大于0");
            }
            updateWrapper.set(UserNutritionGoalPO::getCarbsTarget, command.getCarbsTarget());
            hasUpdate = true;
        }
        if (command.getFatTarget() != null) {
            if (command.getFatTarget().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.INVALID_NUTRITION_VALUE, "脂肪目标必须大于0");
            }
            updateWrapper.set(UserNutritionGoalPO::getFatTarget, command.getFatTarget());
            hasUpdate = true;
        }

        return hasUpdate;
    }

    private UserNutritionGoalPO buildNutritionGoalPO(UpdateNutritionGoalCommand command, boolean useDefaults) {
        UserNutritionGoalPO po = new UserNutritionGoalPO();
        po.setUserId(command.getUserId());
        BigDecimal calorieTarget = command.getCalorieTarget();
        BigDecimal proteinTarget = command.getProteinTarget();
        BigDecimal carbsTarget = command.getCarbsTarget();
        BigDecimal fatTarget = command.getFatTarget();

        if (calorieTarget != null && calorieTarget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_NUTRITION_VALUE, "卡路里目标必须大于0");
        }
        if (proteinTarget != null && proteinTarget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_NUTRITION_VALUE, "蛋白质目标必须大于0");
        }
        if (carbsTarget != null && carbsTarget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_NUTRITION_VALUE, "碳水目标必须大于0");
        }
        if (fatTarget != null && fatTarget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_NUTRITION_VALUE, "脂肪目标必须大于0");
        }

        po.setCalorieTarget(calorieTarget != null ? calorieTarget : (useDefaults ? DEFAULT_CALORIE_TARGET : null));
        po.setProteinTarget(proteinTarget != null ? proteinTarget : (useDefaults ? DEFAULT_PROTEIN_TARGET : null));
        po.setCarbsTarget(carbsTarget != null ? carbsTarget : (useDefaults ? DEFAULT_CARBS_TARGET : null));
        po.setFatTarget(fatTarget != null ? fatTarget : (useDefaults ? DEFAULT_FAT_TARGET : null));
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        return po;
    }

    private NutritionGoalResponse toNutritionGoalResponse(UserNutritionGoalPO po) {
        if (po == null) {
            return null;
        }

        NutritionGoalResponse response = new NutritionGoalResponse();
        response.setId(po.getId());
        response.setUserId(po.getUserId());
        response.setCalorieTarget(po.getCalorieTarget());
        response.setProteinTarget(po.getProteinTarget());
        response.setCarbsTarget(po.getCarbsTarget());
        response.setFatTarget(po.getFatTarget());
        response.setCreatedAt(po.getCreatedAt());
        response.setUpdatedAt(po.getUpdatedAt());
        return response;
    }

    private BodyMetricResponse toBodyMetricResponse(UserBodyMetricPO po) {
        if (po == null) {
            return null;
        }
        return BodyMetricResponse.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .recordDate(po.getRecordDate())
                .weightKg(po.getWeightKg())
                .waistCm(po.getWaistCm())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private UserSettingsPO getOrCreateSettings(Long userId) {
        UserSettingsPO settings = userSettingsMapper.selectByUserId(userId);
        if (settings != null) {
            return settings;
        }

        UserSettingsPO created = buildDefaultSettings(userId, LocalDateTime.now());
        userSettingsMapper.insert(created);
        return created;
    }

    private UserSettingsPO buildDefaultSettings(Long userId, LocalDateTime now) {
        UserSettingsPO po = new UserSettingsPO();
        po.setUserId(userId);
        po.setAllowDataAnalysis(DEFAULT_ALLOW_DATA_ANALYSIS);
        po.setAllowPersonalization(DEFAULT_ALLOW_PERSONALIZATION);
        po.setCreatedAt(now);
        po.setUpdatedAt(now);
        return po;
    }

    private void applySettingsUpdate(UserSettingsPO target, UpdateUserSettingsCommand command) {
        if (command.getAllowDataAnalysis() != null) {
            target.setAllowDataAnalysis(command.getAllowDataAnalysis());
        }
        if (command.getAllowPersonalization() != null) {
            target.setAllowPersonalization(command.getAllowPersonalization());
        }
    }

    private UserSettingsResponse toUserSettingsResponse(UserSettingsPO po) {
        if (po == null) {
            return null;
        }
        return UserSettingsResponse.builder()
                .userId(po.getUserId())
                .allowDataAnalysis(po.getAllowDataAnalysis())
                .allowPersonalization(po.getAllowPersonalization())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private String resolveExtension(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            case "application/pdf" -> "pdf";
            default -> null;
        };
    }
}
