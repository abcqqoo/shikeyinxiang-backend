package com.example.diet.record.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Recommended recipe diet record persistence object.
 */
@Data
@TableName("recommended_recipe_diet_records")
public class RecommendedRecipeDietRecordPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalTime time;
    private String mealType;
    private String remark;
    private String recipeName;
    private String ingredients;
    private String instructions;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
