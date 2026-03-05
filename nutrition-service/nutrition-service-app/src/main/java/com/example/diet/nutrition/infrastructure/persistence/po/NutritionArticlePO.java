package com.example.diet.nutrition.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 营养文章持久化对象
 * 对应数据库 nutrition_article 表
 */
@Data
@TableName("nutrition_article")
public class NutritionArticlePO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String cover;

    private String summary;

    private String content;

    private Integer status;

    private LocalDateTime publishAt;

    private Integer viewCount;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
