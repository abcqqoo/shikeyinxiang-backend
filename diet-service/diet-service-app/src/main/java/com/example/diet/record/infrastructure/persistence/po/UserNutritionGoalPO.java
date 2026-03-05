package com.example.diet.record.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户营养目标持久化对象
 * 对应数据库 user_nutrition_goals 表
 */
@Data
@TableName("user_nutrition_goals")
public class UserNutritionGoalPO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal calorieTarget;

    private BigDecimal proteinTarget;

    private BigDecimal carbsTarget;

    private BigDecimal fatTarget;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
