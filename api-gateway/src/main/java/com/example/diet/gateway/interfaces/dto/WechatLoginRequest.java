package com.example.diet.gateway.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求 DTO
 */
@Data
public class WechatLoginRequest {

    /**
     * 微信小程序登录 code
     */
    @NotBlank(message = "code 不能为空")
    private String code;

    /**
     * 加密的用户数据（用于获取 unionId 等）
     */
    private String encryptedData;

    /**
     * 解密向量
     */
    private String iv;

    /**
     * 用户信息原始数据（可选）
     */
    private String rawData;

    /**
     * 签名（用于验证 rawData）
     */
    private String signature;
}
