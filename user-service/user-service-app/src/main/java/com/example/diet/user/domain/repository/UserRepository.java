package com.example.diet.user.domain.repository;

import com.example.diet.user.domain.model.*;

import java.util.Optional;

/**
 * 用户仓储接口
 * 定义在领域层，实现在基础设施层
 */
public interface UserRepository {

    /**
     * 根据 ID 查找用户
     */
    Optional<User> findById(UserId id);

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(Username username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(Email email);

    /**
     * 根据微信 OpenID 查找用户
     */
    Optional<User> findByOpenid(String openid);

    /**
     * 保存用户（新增或更新）
     */
    void save(User user);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(Username username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(Email email);

    /**
     * 检查 OpenID 是否存在
     */
    boolean existsByOpenid(String openid);

    /**
     * 获取用户总数
     */
    long count();
}
