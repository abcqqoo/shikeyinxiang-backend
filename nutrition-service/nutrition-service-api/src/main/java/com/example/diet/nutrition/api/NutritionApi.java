package com.example.diet.nutrition.api;

import com.example.diet.nutrition.api.command.CreateNutritionAdviceCommand;
import com.example.diet.nutrition.api.command.CreateNutritionArticleCommand;
import com.example.diet.nutrition.api.command.DeleteNutritionAdviceCommand;
import com.example.diet.nutrition.api.command.DeleteNutritionArticleCommand;
import com.example.diet.nutrition.api.command.UpdateNutritionAdviceCommand;
import com.example.diet.nutrition.api.command.UpdateNutritionArticleCommand;
import com.example.diet.nutrition.api.query.*;
import com.example.diet.nutrition.api.response.*;
import com.example.diet.shared.response.PageResponse;

import java.util.List;

/**
 * 营养分析服务 Dubbo API
 */
public interface NutritionApi {

    /**
     * 获取用户营养统计
     */
    NutritionStatsResponse getNutritionStats(GetNutritionStatsQuery query);

    /**
     * 获取每日营养汇总
     */
    DailyNutritionSummaryResponse getDailySummary(GetDailySummaryQuery query);

    /**
     * 获取营养趋势
     */
    NutritionTrendResponse getNutritionTrend(GetNutritionTrendQuery query);

    /**
     * 获取周营养趋势
     */
    List<DailyNutritionResponse> getWeeklyTrend(GetWeeklyTrendQuery query);

    /**
     * 获取营养建议
     */
    List<NutritionAdviceResponse> getAdvice(GetNutritionAdviceQuery query);

    /**
     * 分页查询营养建议
     */
    PageResponse<NutritionAdviceResponse> listAdvice(ListNutritionAdviceQuery query);

    /**
     * 创建营养建议
     */
    NutritionAdviceResponse createAdvice(CreateNutritionAdviceCommand command);

    /**
     * 更新营养建议
     */
    NutritionAdviceResponse updateAdvice(UpdateNutritionAdviceCommand command);

    /**
     * 删除营养建议
     */
    void deleteAdvice(DeleteNutritionAdviceCommand command);

    /**
     * 获取健康报告
     */
    HealthReportResponse getHealthReport(GetHealthReportQuery query);

    // ==================== 营养文章管理 ====================

    /**
     * 分页查询营养文章（后台管理）
     */
    PageResponse<NutritionArticleResponse> listArticles(ListNutritionArticleQuery query);

    /**
     * 创建营养文章
     */
    NutritionArticleResponse createArticle(CreateNutritionArticleCommand command);

    /**
     * 更新营养文章
     */
    NutritionArticleResponse updateArticle(UpdateNutritionArticleCommand command);

    /**
     * 删除营养文章
     */
    void deleteArticle(DeleteNutritionArticleCommand command);

    /**
     * 分页查询已发布文章（小程序端）
     */
    PageResponse<NutritionArticleResponse> listPublishedArticles(ListPublishedArticleQuery query);

    /**
     * 获取文章详情
     */
    NutritionArticleResponse getArticleDetail(GetArticleDetailQuery query);
}
