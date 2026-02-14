package com.example.diet.record.infrastructure.dubbo;

import com.example.diet.record.api.DietRecordApi;
import com.example.diet.record.api.command.CreateDietRecordCommand;
import com.example.diet.record.api.command.DeleteDietRecordCommand;
import com.example.diet.record.api.command.UpdateDietRecordCommand;
import com.example.diet.record.api.query.GetDailyNutritionSummaryQuery;
import com.example.diet.record.api.query.GetDietRecordQuery;
import com.example.diet.record.api.query.GetNutritionComplianceRateQuery;
import com.example.diet.record.api.query.GetNutritionTrendSummaryQuery;
import com.example.diet.record.api.query.GetPopularFoodStatsQuery;
import com.example.diet.record.api.query.GetRecordsByDateQuery;
import com.example.diet.record.api.query.ListCommonMealTemplatesQuery;
import com.example.diet.record.api.query.ListDietRecordsQuery;
import com.example.diet.record.api.response.CommonMealTemplateResponse;
import com.example.diet.record.api.response.DailyNutritionSummaryResponse;
import com.example.diet.record.api.response.DietRecordResponse;
import com.example.diet.record.api.response.NutritionComplianceRateResponse;
import com.example.diet.record.api.response.NutritionTrendSummaryResponse;
import com.example.diet.record.api.response.PopularFoodStatResponse;
import com.example.diet.record.application.DietRecordApplicationService;
import com.example.diet.shared.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDate;
import java.util.List;

/**
 * 饮食记录 Dubbo API 实现
 */
@DubboService
@RequiredArgsConstructor
public class DietRecordApiImpl implements DietRecordApi {

    private final DietRecordApplicationService applicationService;

    @Override
    public DietRecordResponse createRecord(CreateDietRecordCommand command) {
        return applicationService.createRecord(command);
    }

    @Override
    public DietRecordResponse updateRecord(UpdateDietRecordCommand command) {
        return applicationService.updateRecord(command);
    }

    @Override
    public void deleteRecord(DeleteDietRecordCommand command) {
        applicationService.deleteRecord(command);
    }

    @Override
    public DietRecordResponse getRecord(GetDietRecordQuery query) {
        return applicationService.getRecord(query);
    }

    @Override
    public PageResponse<DietRecordResponse> listRecords(ListDietRecordsQuery query) {
        return applicationService.listRecords(query);
    }

    @Override
    public List<DietRecordResponse> getRecordsByDate(GetRecordsByDateQuery query) {
        return applicationService.getRecordsByDate(query);
    }

    @Override
    public DailyNutritionSummaryResponse getDailyNutritionSummary(GetDailyNutritionSummaryQuery query) {
        return applicationService.getDailyNutritionSummary(query);
    }

    @Override
    public int countRecordsByDate(LocalDate date) {
        return applicationService.countRecordsByDate(date);
    }

    @Override
    public int countRecommendedRecipeRecordsByDate(LocalDate date) {
        return applicationService.countRecommendedRecipeRecordsByDate(date);
    }

    @Override
    public NutritionTrendSummaryResponse getNutritionTrendSummary(GetNutritionTrendSummaryQuery query) {
        return applicationService.getNutritionTrendSummary(query);
    }

    @Override
    public NutritionComplianceRateResponse getNutritionComplianceRate(GetNutritionComplianceRateQuery query) {
        return applicationService.getNutritionComplianceRate(query);
    }

    @Override
    public List<PopularFoodStatResponse> getPopularFoodStats(GetPopularFoodStatsQuery query) {
        return applicationService.getPopularFoodStats(query);
    }

    @Override
    public List<CommonMealTemplateResponse> listCommonMealTemplates(ListCommonMealTemplatesQuery query) {
        return applicationService.listCommonMealTemplates(query);
    }
}
