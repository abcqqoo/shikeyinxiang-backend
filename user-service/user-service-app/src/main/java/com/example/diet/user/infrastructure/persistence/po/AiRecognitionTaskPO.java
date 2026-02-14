package com.example.diet.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI食物识别任务持久化对象
 * 对应数据库 ai_recognition_task 表
 */
@Data
@TableName("ai_recognition_task")
public class AiRecognitionTaskPO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发起识别的用户ID
     */
    private Long userId;

    /**
     * 原始图片URL（如已存储）
     */
    private String imageUrl;

    /**
     * 状态: pending/processing/completed/failed
     */
    private String status;

    /**
     * 使用的AI模型（如 gemini-2.0-flash）
     */
    private String modelName;

    /**
     * 识别出的食物数量
     */
    private Integer totalItems;

    /**
     * 处理耗时（毫秒）
     */
    private Integer processingTimeMs;

    /**
     * 失败时的错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
