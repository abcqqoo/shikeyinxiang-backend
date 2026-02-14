package com.example.diet.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI食物识别结果项持久化对象
 * 对应数据库 ai_recognition_item 表
 */
@Data
@TableName("ai_recognition_item")
public class AiRecognitionItemPO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的识别任务ID
     */
    private Long taskId;

    /**
     * 识别出的食物名称
     */
    private String foodName;

    /**
     * 置信度 0.0000-1.0000
     */
    private BigDecimal confidence;

    /**
     * 估算热量（千卡）
     */
    private BigDecimal calories;

    /**
     * 蛋白质（克）
     */
    private BigDecimal proteinG;

    /**
     * 脂肪（克）
     */
    private BigDecimal fatG;

    /**
     * 碳水化合物（克）
     */
    private BigDecimal carbsG;

    /**
     * 估算份量（克）
     */
    private Integer estimatedGrams;

    /**
     * 用户是否选择了此项
     */
    private Boolean wasSelected;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
