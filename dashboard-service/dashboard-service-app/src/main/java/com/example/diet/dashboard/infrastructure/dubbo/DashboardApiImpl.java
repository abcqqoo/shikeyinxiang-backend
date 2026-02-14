package com.example.diet.dashboard.infrastructure.dubbo;

import com.example.diet.dashboard.api.DashboardApi;
import com.example.diet.dashboard.api.query.*;
import com.example.diet.dashboard.api.response.*;
import com.example.diet.record.api.DietRecordApi;
import com.example.diet.record.api.query.GetNutritionComplianceRateQuery;
import com.example.diet.record.api.query.GetNutritionTrendSummaryQuery;
import com.example.diet.record.api.query.GetPopularFoodStatsQuery;
import com.example.diet.record.api.query.ListDietRecordsQuery;
import com.example.diet.record.api.response.DietRecordResponse;
import com.example.diet.record.api.response.NutritionComplianceRateResponse;
import com.example.diet.record.api.response.NutritionTrendSummaryResponse;
import com.example.diet.record.api.response.PopularFoodStatResponse;
import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.UserApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 仪表盘服务 Dubbo API 实现
 * 聚合多个服务的数据提供给管理员仪表盘
 */
@Slf4j
@DubboService
public class DashboardApiImpl implements DashboardApi {
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private DietRecordApi dietRecordApi;

    @Override
    @Cacheable(
            value = "dashboard",
            key = "'stats_' + (#query.date == null ? T(java.time.LocalDate).now() : #query.date)"
    )
    public DashboardStatsResponse getStats(GetDashboardStatsQuery query) {
        log.debug("获取仪表盘统计数据, query: {}", query);

        LocalDate date = query.getDate() != null ? query.getDate() : LocalDate.now();

        try {
            // 获取总用户数
            long totalUsers = userApi.countUsers();

            // 获取指定日期的饮食记录数
            int todayRecords = dietRecordApi.countRecordsByDate(date);
            int recommendedRecipeRecords = dietRecordApi.countRecommendedRecipeRecordsByDate(date);

            NutritionComplianceRateResponse complianceResponse = dietRecordApi.getNutritionComplianceRate(
                    GetNutritionComplianceRateQuery.builder().date(date).build());
            double complianceRate = complianceResponse != null && complianceResponse.getComplianceRate() != null
                    ? complianceResponse.getComplianceRate()
                    : 0.0;
            double recommendationAdoptionRate = todayRecords == 0
                    ? 0.0
                    : BigDecimal.valueOf(recommendedRecipeRecords * 100.0)
                            .divide(BigDecimal.valueOf(todayRecords), 2, RoundingMode.HALF_UP)
                            .doubleValue();

            return DashboardStatsResponse.builder()
                    .totalUsers(totalUsers)
                    .todayRecords(todayRecords)
                    .nutritionComplianceRate(complianceRate)
                    .recommendationAdoptionRate(recommendationAdoptionRate)
                    .statisticsDate(date)
                    .build();

        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            throw new RuntimeException("获取统计数据失败", e);
        }
    }

    @Override
    @Cacheable(
            value = "dashboard",
            key = "'trend_' + (#query.period == null ? 'week' : #query.period.toLowerCase())"
    )
    public NutritionTrendResponse getNutritionTrend(GetNutritionTrendQuery query) {
        log.debug("获取营养趋势, query: {}", query);

        try {
            String period = query.getPeriod() != null ? query.getPeriod() : "week";

            NutritionTrendSummaryResponse summary = dietRecordApi.getNutritionTrendSummary(
                    GetNutritionTrendSummaryQuery.builder().period(period).build());

            List<String> dateList = new ArrayList<>();
            List<Double> calorieList = toDoubleList(summary != null ? summary.getCalorieList() : null);
            List<Double> proteinList = toDoubleList(summary != null ? summary.getProteinList() : null);
            List<Double> carbsList = toDoubleList(summary != null ? summary.getCarbsList() : null);
            List<Double> fatList = toDoubleList(summary != null ? summary.getFatList() : null);

            if (summary != null && summary.getDateList() != null) {
                for (String date : summary.getDateList()) {
                    dateList.add(formatDateLabel(date));
                }
            }

            return NutritionTrendResponse.builder()
                    .period(period)
                    .dataPoints(dateList.size())
                    .dateList(dateList)
                    .calorieList(calorieList)
                    .proteinList(proteinList)
                    .carbsList(carbsList)
                    .fatList(fatList)
                    .build();

        } catch (Exception e) {
            log.error("获取营养趋势失败", e);
            throw new RuntimeException("获取营养趋势失败", e);
        }
    }

    @Override
    public PageResponse<DietRecordResponse> getLatestRecords(ListLatestRecordsQuery query) {
        log.debug("获取最新饮食记录, query: {}", query);

        try {
            ListDietRecordsQuery listQuery = ListDietRecordsQuery.builder()
                    .userId(query.getUserId())
                    .page(query.getPage())
                    .size(query.getSize())
                    .build();

            return dietRecordApi.listRecords(listQuery);

        } catch (Exception e) {
            log.error("获取最新饮食记录失败", e);
            throw new RuntimeException("获取饮食记录失败", e);
        }
    }

    @Override
    @Cacheable(
            value = "dashboard",
            key = "'popular_' + (#query.period == null ? 'week' : #query.period.toLowerCase()) + '_' + (#query.limit == null ? 10 : #query.limit)"
    )
    public List<PopularFoodResponse> getPopularFoods(GetPopularFoodsQuery query) {
        log.debug("获取热门食物, query: {}", query);

        try {
            String period = query.getPeriod() != null ? query.getPeriod() : "week";
            int limit = query.getLimit() != null ? query.getLimit() : 10;

            List<PopularFoodStatResponse> stats = dietRecordApi.getPopularFoodStats(
                    GetPopularFoodStatsQuery.builder().period(period).limit(limit).build());

            List<PopularFoodResponse> result = new ArrayList<>();
            for (PopularFoodStatResponse stat : stats) {
                result.add(PopularFoodResponse.builder()
                        .foodId(stat.getFoodId())
                        .name(stat.getName())
                        .count(stat.getCount())
                        .isRecipe(stat.getIsRecipe())
                        .build());
            }

            return result;

        } catch (Exception e) {
            log.error("获取热门食物失败", e);
            throw new RuntimeException("获取热门食物失败", e);
        }
    }

    private List<Double> toDoubleList(List<BigDecimal> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        List<Double> result = new ArrayList<>(values.size());
        for (BigDecimal value : values) {
            result.add(value != null ? value.doubleValue() : 0.0);
        }
        return result;
    }

    private String formatDateLabel(String date) {
        if (date == null || date.isBlank()) {
            return "";
        }
        if (!ISO_DATE_PATTERN.matcher(date).matches()) {
            return date;
        }
        LocalDate parsed = LocalDate.parse(date);
        return parsed.format(DateTimeFormatter.ofPattern("MM-dd"));
    }
}
