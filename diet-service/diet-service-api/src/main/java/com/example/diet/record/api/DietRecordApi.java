package com.example.diet.record.api;

import com.example.diet.record.api.command.*;
import com.example.diet.record.api.query.*;
import com.example.diet.record.api.response.*;
import com.example.diet.shared.response.PageResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * 饮食记录服务 Dubbo API
 */
public interface DietRecordApi {

    // ==================== 命令操作 ====================

    /**
     * 创建饮食记录
     */
    DietRecordResponse createRecord(CreateDietRecordCommand command);

    /**
     * 更新饮食记录
     */
    DietRecordResponse updateRecord(UpdateDietRecordCommand command);

    /**
     * 删除饮食记录
     */
    void deleteRecord(DeleteDietRecordCommand command);

    // ==================== 查询操作 ====================

    /**
     * 获取饮食记录详情
     */
    DietRecordResponse getRecord(GetDietRecordQuery query);

    /**
     * 分页查询用户饮食记录
     */
    PageResponse<DietRecordResponse> listRecords(ListDietRecordsQuery query);

    /**
     * 获取用户某日饮食记录
     */
    List<DietRecordResponse> getRecordsByDate(GetRecordsByDateQuery query);

    /**
     * 获取用户每日营养汇总
     */
    DailyNutritionSummaryResponse getDailyNutritionSummary(GetDailyNutritionSummaryQuery query);

    /**
     * 统计指定日期的饮食记录数
     */
    int countRecordsByDate(LocalDate date);

    /**
     * 统计指定日期推荐菜谱记录数
     */
    int countRecommendedRecipeRecordsByDate(LocalDate date);

    /**
     * 获取全站营养趋势汇总
     */
    NutritionTrendSummaryResponse getNutritionTrendSummary(GetNutritionTrendSummaryQuery query);

    /**
     * 获取营养达标率
     */
    NutritionComplianceRateResponse getNutritionComplianceRate(GetNutritionComplianceRateQuery query);

    /**
     * 获取热门食物统计
     */
    List<PopularFoodStatResponse> getPopularFoodStats(GetPopularFoodStatsQuery query);

    /**
     * 获取用户常用餐模板
     */
    List<CommonMealTemplateResponse> listCommonMealTemplates(ListCommonMealTemplatesQuery query);
}
