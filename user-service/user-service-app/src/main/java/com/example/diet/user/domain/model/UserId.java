package com.example.diet.user.domain.model;

import com.example.diet.shared.ddd.Identifier;

/**
 * 用户 ID 值对象
 */
public class UserId extends Identifier<Long> {

    public UserId(Long value) {
        super(value);
    }

    public static UserId of(Long value) {
        return new UserId(value);
    }

    public static UserId generate() {
        // 生产环境应使用雪花算法等分布式 ID 生成器
        return new UserId(System.nanoTime());
    }
}
