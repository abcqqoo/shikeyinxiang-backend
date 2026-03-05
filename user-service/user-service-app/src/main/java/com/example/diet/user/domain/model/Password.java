package com.example.diet.user.domain.model;

import com.example.diet.shared.ddd.ValueObject;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码值对象
 * 封装密码的加密和验证逻辑
 */
public record Password(String hashedValue) implements ValueObject {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 32;

    /**
     * 从明文密码创建（会进行加密）
     */
    public static Password fromPlainText(String plainText) {
        validateStrength(plainText);
        return new Password(ENCODER.encode(plainText));
    }

    /**
     * 从已加密的密码重建（不进行加密，用于从数据库加载）
     */
    public static Password fromHashed(String hashedValue) {
        return new Password(hashedValue);
    }

    /**
     * 验证明文密码是否匹配
     */
    public boolean matches(String plainText) {
        if (plainText == null || hashedValue == null) {
            return false;
        }
        return ENCODER.matches(plainText, hashedValue);
    }

    /**
     * 验证密码强度
     */
    private static void validateStrength(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "密码不能为空");
        }
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD,
                    String.format("密码长度必须在 %d-%d 个字符之间", MIN_LENGTH, MAX_LENGTH));
        }
        // 可以添加更多密码强度规则：大小写、数字、特殊字符等
    }
}
