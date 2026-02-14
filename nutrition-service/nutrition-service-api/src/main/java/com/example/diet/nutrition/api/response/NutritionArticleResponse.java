package com.example.diet.nutrition.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 营养文章响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionArticleResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String cover;

    private String coverKey;

    private String summary;

    private String content;

    private Integer status;

    private LocalDateTime publishAt;

    private Integer viewCount;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
