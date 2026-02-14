package com.example.diet.record.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 饮食记录食物明细持久化对象
 */
@Data
@TableName("diet_record_foods")
public class DietRecordFoodPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dietRecordId;
    private Long foodId;
    private String foodName;
    private BigDecimal amount;
    private String unit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    /**
     * database / ai_estimated
     */
    private String source;
    private BigDecimal grams;
    private LocalDateTime createdAt;
}
