package com.example.diet.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户体重/腰围记录持久化对象.
 */
@Data
@TableName("user_body_metrics")
public class UserBodyMetricPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private LocalDate recordDate;
    private BigDecimal weightKg;
    private BigDecimal waistCm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
