package com.example.diet.user.infrastructure.persistence;

import com.example.diet.user.domain.model.*;
import com.example.diet.user.domain.repository.UserRepository;
import com.example.diet.user.infrastructure.persistence.converter.UserConverter;
import com.example.diet.user.infrastructure.persistence.mapper.UserMapper;
import com.example.diet.user.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    @Override
    public Optional<User> findById(UserId id) {
        UserPO po = userMapper.selectById(id.getValue());
        return Optional.ofNullable(userConverter.toDomain(po));
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        UserPO po = userMapper.selectByUsername(username.value());
        return Optional.ofNullable(userConverter.toDomain(po));
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        UserPO po = userMapper.selectByEmail(email.value());
        return Optional.ofNullable(userConverter.toDomain(po));
    }

    @Override
    public Optional<User> findByOpenid(String openid) {
        UserPO po = userMapper.selectByOpenid(openid);
        return Optional.ofNullable(userConverter.toDomain(po));
    }

    @Override
    public void save(User user) {
        UserPO po = userConverter.toPO(user);
        UserPO existing = userMapper.selectById(user.getId().getValue());

        if (existing == null) {
            // 新增时不设置 ID，让数据库自动生成
            po.setId(null);
            userMapper.insert(po);
        } else {
            userMapper.updateById(po);
        }
    }

    @Override
    public boolean existsByUsername(Username username) {
        return userMapper.countByUsername(username.value()) > 0;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userMapper.countByEmail(email.value()) > 0;
    }

    @Override
    public boolean existsByOpenid(String openid) {
        return userMapper.countByOpenid(openid) > 0;
    }

    @Override
    public long count() {
        return userMapper.selectCount(null);
    }
}
