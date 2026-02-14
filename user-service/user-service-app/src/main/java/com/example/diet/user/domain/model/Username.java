package com.example.diet.user.domain.model;

import com.example.diet.shared.ddd.ValueObject;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;

import java.util.regex.Pattern;

/**
 * 用户名值对象
 */
public record Username(String value) implements ValueObject {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 20;
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$");

    public Username {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户名不能为空");
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER,
                    String.format("用户名长度必须在 %d-%d 个字符之间", MIN_LENGTH, MAX_LENGTH));
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER,
                    "用户名只能包含字母、数字、下划线和中文");
        }
    }

    public static Username of(String value) {
        return new Username(value);
    }
}
