package com.example.diet.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 头像 URL 响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUrlResponse implements Serializable {

    /**
     * 预签名 URL (上传或下载)
     */
    private String url;

    /**
     * 头像存储 Key
     */
    private String key;
}
