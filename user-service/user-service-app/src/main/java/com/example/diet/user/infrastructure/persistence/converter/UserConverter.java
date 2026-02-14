package com.example.diet.user.infrastructure.persistence.converter;

import com.example.diet.user.domain.model.*;
import com.example.diet.user.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Component;

/**
 * 用户转换器
 * 负责领域对象与持久化对象之间的转换
 */
@Component
public class UserConverter {

    /**
     * 持久化对象 -> 领域对象
     */
    public User toDomain(UserPO po) {
        if (po == null) {
            return null;
        }

        return User.reconstitute(
                UserId.of(po.getId()),
                Username.of(po.getUsername()),
                po.getEmail() != null ? Email.of(po.getEmail()) : null,
                po.getPassword() != null ? Password.fromHashed(po.getPassword()) : null,
                UserRole.of(po.getRole()),
                UserStatus.of(po.getStatus()),
                po.getAvatarUrl(),
                po.getOpenid(),
                po.getCreateTime()
        );
    }

    /**
     * 领域对象 -> 持久化对象
     */
    public UserPO toPO(User user) {
        if (user == null) {
            return null;
        }

        UserPO po = new UserPO();
        po.setId(user.getId().getValue());
        po.setUsername(user.getUsername().value());
        po.setEmail(user.getEmail() != null ? user.getEmail().value() : null);
        po.setPassword(user.getPassword() != null ? user.getPassword().hashedValue() : null);
        po.setRole(user.getRole().name());
        po.setStatus(user.getStatus().getValue());
        po.setAvatarUrl(user.getAvatarUrl());
        po.setOpenid(user.getOpenid());
        po.setCreateTime(user.getCreateTime());
        return po;
    }
}
