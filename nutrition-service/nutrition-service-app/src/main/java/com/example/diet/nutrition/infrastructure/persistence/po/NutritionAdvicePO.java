package com.example.diet.nutrition.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 营养建议持久化对象
 * 对应数据库 nutrition_advice 表
 */
@Data
@TableName("nutrition_advice")
public class NutritionAdvicePO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String title;

    private String description;

    private String conditionType;

    private Integer minPercentage;

    private Integer maxPercentage;

    private Boolean isDefault;

    private Integer priority;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
