package com.example.diet.user.domain.model;

import com.example.diet.shared.ddd.ValueObject;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;

import java.util.regex.Pattern;

/**
 * 邮箱值对象
 */
public record Email(String value) implements ValueObject {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "邮箱格式不正确");
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    /**
     * 获取邮箱域名
     */
    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }
}
