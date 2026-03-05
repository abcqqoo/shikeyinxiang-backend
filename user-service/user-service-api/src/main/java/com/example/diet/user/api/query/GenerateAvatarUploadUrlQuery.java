package com.example.diet.user.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成头像上传 URL 查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateAvatarUploadUrlQuery implements Query {

    private Long userId;

    /**
     * 文件内容类型 (如 image/jpeg, image/png)
     */
    private String contentType;
}
