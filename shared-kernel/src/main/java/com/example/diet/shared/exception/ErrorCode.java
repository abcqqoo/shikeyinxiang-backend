package com.example.diet.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 错误码枚举
 * 统一管理系统所有错误码
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ==================== 通用错误 1xxx ====================
    INVALID_PARAMETER(1001, "参数错误"),
    RESOURCE_NOT_FOUND(1002, "资源不存在"),
    OPERATION_NOT_ALLOWED(1003, "操作不允许"),
    DUPLICATE_RESOURCE(1004, "资源已存在"),
    INTERNAL_ERROR(1500, "系统内部错误"),

    // ==================== 用户相关 2xxx ====================
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_EXISTS(2002, "用户名已存在"),
    EMAIL_EXISTS(2003, "邮箱已存在"),
    INVALID_PASSWORD(2004, "密码不符合要求"),
    PASSWORD_MISMATCH(2005, "密码错误"),
    USER_DISABLED(2006, "用户已禁用"),
    OPENID_EXISTS(2007, "微信账号已绑定"),

    // ==================== 认证相关 3xxx ====================
    UNAUTHORIZED(3001, "未登录"),
    TOKEN_EXPIRED(3002, "Token 已过期"),
    TOKEN_INVALID(3003, "Token 无效"),
    ACCESS_DENIED(3004, "无权限访问"),
    LOGIN_FAILED(3005, "登录失败"),

    // ==================== 饮食记录相关 4xxx ====================
    DIET_RECORD_NOT_FOUND(4001, "饮食记录不存在"),
    MAX_FOODS_EXCEEDED(4002, "单餐食物数量超限"),
    INVALID_MEAL_TYPE(4003, "无效的餐次类型"),
    INVALID_DATE_RANGE(4004, "日期范围无效"),

    // ==================== 食物相关 5xxx ====================
    FOOD_NOT_FOUND(5001, "食物不存在"),
    FOOD_CATEGORY_NOT_FOUND(5002, "食物分类不存在"),
    FOOD_CATEGORY_HAS_CHILDREN(5003, "分类下存在子分类或食物"),

    // ==================== 营养相关 6xxx ====================
    NUTRITION_GOAL_NOT_FOUND(6001, "营养目标不存在"),
    INVALID_NUTRITION_VALUE(6002, "营养值无效"),

    // ==================== 文件相关 7xxx ====================
    FILE_NOT_FOUND(7001, "文件不存在"),
    FILE_UPLOAD_FAILED(7002, "文件上传失败"),
    INVALID_FILE_TYPE(7003, "文件类型不支持"),
    FILE_TOO_LARGE(7004, "文件过大");

    private final int code;
    private final String message;
}
