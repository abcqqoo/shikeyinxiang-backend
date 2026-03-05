package com.example.diet.nutrition.infrastructure.dubbo;

import com.example.diet.nutrition.api.NutritionApi;
import com.example.diet.nutrition.api.command.CreateNutritionAdviceCommand;
import com.example.diet.nutrition.api.command.CreateNutritionArticleCommand;
import com.example.diet.nutrition.api.command.DeleteNutritionAdviceCommand;
import com.example.diet.nutrition.api.command.DeleteNutritionArticleCommand;
import com.example.diet.nutrition.api.command.UpdateNutritionAdviceCommand;
import com.example.diet.nutrition.api.command.UpdateNutritionArticleCommand;
import com.example.diet.nutrition.api.query.GetArticleDetailQuery;
import com.example.diet.nutrition.api.query.GetDailySummaryQuery;
import com.example.diet.nutrition.api.query.GetHealthReportQuery;
import com.example.diet.nutrition.api.query.GetNutritionAdviceQuery;
import com.example.diet.nutrition.api.query.GetNutritionStatsQuery;
import com.example.diet.nutrition.api.query.GetNutritionTrendQuery;
import com.example.diet.nutrition.api.query.GetWeeklyTrendQuery;
import com.example.diet.nutrition.api.query.ListNutritionAdviceQuery;
import com.example.diet.nutrition.api.query.ListNutritionArticleQuery;
import com.example.diet.nutrition.api.query.ListPublishedArticleQuery;
import com.example.diet.nutrition.api.response.DailyNutritionResponse;
import com.example.diet.nutrition.api.response.HealthReportResponse;
import com.example.diet.nutrition.api.response.NutritionAdviceResponse;
import com.example.diet.nutrition.api.response.NutritionArticleResponse;
import com.example.diet.nutrition.api.response.NutritionStatsResponse;
import com.example.diet.nutrition.api.response.NutritionTrendResponse;
import com.example.diet.nutrition.application.NutritionApplicationService;
import com.example.diet.shared.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 营养分析 Dubbo API 实现
 */
@DubboService
@RequiredArgsConstructor
public class NutritionApiImpl implements NutritionApi {

    private final NutritionApplicationService applicationService;

    @Override
    public NutritionStatsResponse getNutritionStats(GetNutritionStatsQuery query) {
        return applicationService.getNutritionStats(query);
    }

    @Override
    public com.example.diet.nutrition.api.response.DailyNutritionSummaryResponse getDailySummary(GetDailySummaryQuery query) {
        return applicationService.getDailySummary(query);
    }

    @Override
    public NutritionTrendResponse getNutritionTrend(GetNutritionTrendQuery query) {
        return applicationService.getNutritionTrend(query);
    }

    @Override
    public List<DailyNutritionResponse> getWeeklyTrend(GetWeeklyTrendQuery query) {
        return applicationService.getWeeklyTrend(query);
    }

    @Override
    public List<NutritionAdviceResponse> getAdvice(GetNutritionAdviceQuery query) {
        return applicationService.getAdvice(query);
    }

    @Override
    public PageResponse<NutritionAdviceResponse> listAdvice(ListNutritionAdviceQuery query) {
        return applicationService.listAdvice(query);
    }

    @Override
    public NutritionAdviceResponse createAdvice(CreateNutritionAdviceCommand command) {
        return applicationService.createAdvice(command);
    }

    @Override
    public NutritionAdviceResponse updateAdvice(UpdateNutritionAdviceCommand command) {
        return applicationService.updateAdvice(command);
    }

    @Override
    public void deleteAdvice(DeleteNutritionAdviceCommand command) {
        applicationService.deleteAdvice(command);
    }

    @Override
    public HealthReportResponse getHealthReport(GetHealthReportQuery query) {
        return applicationService.getHealthReport(query);
    }

    // ==================== 营养文章 ====================

    @Override
    public PageResponse<NutritionArticleResponse> listArticles(ListNutritionArticleQuery query) {
        return applicationService.listArticles(query);
    }

    @Override
    public NutritionArticleResponse createArticle(CreateNutritionArticleCommand command) {
        return applicationService.createArticle(command);
    }

    @Override
    public NutritionArticleResponse updateArticle(UpdateNutritionArticleCommand command) {
        return applicationService.updateArticle(command);
    }

    @Override
    public void deleteArticle(DeleteNutritionArticleCommand command) {
        applicationService.deleteArticle(command);
    }

    @Override
    public PageResponse<NutritionArticleResponse> listPublishedArticles(ListPublishedArticleQuery query) {
        return applicationService.listPublishedArticles(query);
    }

    @Override
    public NutritionArticleResponse getArticleDetail(GetArticleDetailQuery query) {
        return applicationService.getArticleDetail(query);
    }
}
