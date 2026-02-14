package com.example.diet.record.api.command;

import com.example.diet.shared.cqrs.Command;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 创建饮食记录命令
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreateDietRecordCommand implements Command {

    private Long userId;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime time;

    @NotNull(message = "餐次类型不能为空")
    private String mealType;

    private String remark;

    @NotEmpty(message = "食物列表不能为空")
    private List<DietRecordFoodItem> foods;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DietRecordFoodItem implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private Long foodId;

        private String foodName;
        private String name;

        private java.math.BigDecimal amount;

        private String unit;
        private java.math.BigDecimal grams;
        private java.math.BigDecimal calories;
        private java.math.BigDecimal protein;
        private java.math.BigDecimal fat;
        private java.math.BigDecimal carbs;

        /**
         * regular / recipe
         */
        private String type;

        /**
         * 食物来源：
         * - database: 数据库食物（默认）
         * - ai_estimated: AI估算食物
         */
        private String source;

        private String ingredients;
        private String instructions;
    }
}
