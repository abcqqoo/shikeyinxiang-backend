package com.example.diet.user.domain.model;

import com.example.diet.shared.ddd.AggregateRoot;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;
import com.example.diet.user.domain.event.PasswordChangedEvent;
import com.example.diet.user.domain.event.UserCreatedEvent;
import com.example.diet.user.domain.event.UserDisabledEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户聚合根
 * 封装用户的所有业务逻辑
 */
@Getter
public class User extends AggregateRoot<UserId> {

    private Username username;
    private Email email;
    private Password password;
    private UserRole role;
    private UserStatus status;
    private String avatarUrl;
    private String openid;
    private final LocalDateTime createTime;

    // ==================== 私有构造函数 ====================

    private User(UserId id, Username username, Email email, Password password, LocalDateTime createTime) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = UserRole.USER;
        this.status = UserStatus.ACTIVE;
        this.createTime = createTime;
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建普通用户（邮箱注册）
     */
    public static User create(Username username, Email email, Password password) {
        User user = new User(UserId.generate(), username, email, password, LocalDateTime.now());
        user.registerEvent(new UserCreatedEvent(user.id, username, email));
        return user;
    }

    /**
     * 创建微信用户
     */
    public static User createWithOpenid(String openid, Username username) {
        User user = new User(UserId.generate(), username, null, null, LocalDateTime.now());
        user.openid = openid;
        user.registerEvent(new UserCreatedEvent(user.id, username, null));
        return user;
    }

    /**
     * 从持久化数据重建用户（不触发领域事件）
     */
    public static User reconstitute(
            UserId id,
            Username username,
            Email email,
            Password password,
            UserRole role,
            UserStatus status,
            String avatarUrl,
            String openid,
            LocalDateTime createTime
    ) {
        User user = new User(id, username, email, password, createTime);
        user.role = role;
        user.status = status;
        user.avatarUrl = avatarUrl;
        user.openid = openid;
        return user;
    }

    // ==================== 业务行为 ====================

    /**
     * 修改密码
     */
    public void changePassword(Password newPassword) {
        ensureActive();
        this.password = newPassword;
        registerEvent(new PasswordChangedEvent(this.id));
    }

    /**
     * 更新个人信息
     */
    public void updateProfile(Username username, Email email) {
        ensureActive();
        if (username != null) {
            this.username = username;
        }
        if (email != null) {
            this.email = email;
        }
    }

    /**
     * 更新头像
     */
    public void updateAvatar(String avatarUrl) {
        ensureActive();
        this.avatarUrl = avatarUrl;
    }

    /**
     * 禁用用户
     */
    public void disable() {
        if (this.status == UserStatus.DISABLED) {
            return;
        }
        this.status = UserStatus.DISABLED;
        registerEvent(new UserDisabledEvent(this.id));
    }

    /**
     * 启用用户
     */
    public void enable() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String plainPassword) {
        if (this.password == null) {
            return false;
        }
        return this.password.matches(plainPassword);
    }

    /**
     * 绑定微信
     */
    public void bindOpenid(String openid) {
        ensureActive();
        if (this.openid != null && !this.openid.isBlank()) {
            throw new BusinessException(ErrorCode.OPENID_EXISTS, "已绑定微信账号");
        }
        this.openid = openid;
    }

    /**
     * 提升为管理员
     */
    public void promoteToAdmin() {
        this.role = UserRole.ADMIN;
    }

    // ==================== 私有方法 ====================

    private void ensureActive() {
        if (this.status != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
    }
}
