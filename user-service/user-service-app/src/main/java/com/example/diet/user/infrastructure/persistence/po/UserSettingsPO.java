package com.example.diet.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置持久化对象.
 */
@Data
@TableName("user_settings")
public class UserSettingsPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Boolean allowDataAnalysis;
    private Boolean allowPersonalization;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
