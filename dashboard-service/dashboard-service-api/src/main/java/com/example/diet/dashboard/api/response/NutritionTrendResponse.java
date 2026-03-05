package com.example.diet.dashboard.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 营养趋势响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTrendResponse implements Serializable {

    /**
     * 统计周期
     */
    private String period;

    /**
     * 数据点数量
     */
    private Integer dataPoints;

    /**
     * 日期列表
     */
    private List<String> dateList;

    /**
     * 卡路里列表
     */
    private List<Double> calorieList;

    /**
     * 蛋白质列表
     */
    private List<Double> proteinList;

    /**
     * 碳水化合物列表
     */
    private List<Double> carbsList;

    /**
     * 脂肪列表
     */
    private List<Double> fatList;
}
