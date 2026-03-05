package com.example.diet.nutrition.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.diet.file.api.FileApi;
import com.example.diet.file.api.query.GenerateDownloadUrlQuery;
import com.example.diet.file.api.response.PresignedUrlResponse;
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
import com.example.diet.nutrition.infrastructure.persistence.mapper.NutritionAdviceMapper;
import com.example.diet.nutrition.infrastructure.persistence.mapper.NutritionArticleMapper;
import com.example.diet.nutrition.infrastructure.persistence.po.NutritionAdvicePO;
import com.example.diet.nutrition.infrastructure.persistence.po.NutritionArticlePO;
import com.example.diet.record.api.DietRecordApi;
import com.example.diet.record.api.query.GetDailyNutritionSummaryQuery;
import com.example.diet.record.api.response.DailyNutritionSummaryResponse;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.UserApi;
import com.example.diet.user.api.query.GetNutritionGoalQuery;
import com.example.diet.user.api.response.NutritionGoalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 营养分析应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionApplicationService {

    @DubboReference
    private DietRecordApi dietRecordApi;

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private FileApi fileApi;

    private final NutritionAdviceMapper nutritionAdviceMapper;

    private final NutritionArticleMapper nutritionArticleMapper;

    private static final int ARTICLE_MEDIA_DOWNLOAD_EXPIRE_MINUTES = 60;
    private static final Pattern MEDIA_URL_ATTR_PATTERN = Pattern.compile(
            "(\\b(?:src|poster)\\s*=\\s*)(\"([^\"]*)\"|'([^']*)')",
            Pattern.CASE_INSENSITIVE
    );

    public NutritionStatsResponse getNutritionStats(GetNutritionStatsQuery query) {
        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        int days = 0;

        LocalDate current = query.getStartDate();
        while (!current.isAfter(query.getEndDate())) {
            try {
                DailyNutritionSummaryResponse summary = dietRecordApi.getDailyNutritionSummary(
                        GetDailyNutritionSummaryQuery.builder()
                                .userId(query.getUserId())
                                .date(current)
                                .build()
                );

                if (summary != null && summary.getRecordCount() > 0) {
                    days++;
                    totalCalories = totalCalories.add(summary.getTotalCalories() != null ? summary.getTotalCalories() : BigDecimal.ZERO);
                    totalProtein = totalProtein.add(summary.getTotalProtein() != null ? summary.getTotalProtein() : BigDecimal.ZERO);
                    totalFat = totalFat.add(summary.getTotalFat() != null ? summary.getTotalFat() : BigDecimal.ZERO);
                    totalCarbs = totalCarbs.add(summary.getTotalCarbs() != null ? summary.getTotalCarbs() : BigDecimal.ZERO);
                }
            } catch (Exception e) {
                log.warn("action=get_nutrition_stats_daily_summary_failed userId={} date={} fallback=skip_day message={}",
                        query.getUserId(), current, e.getMessage());
            }
            current = current.plusDays(1);
        }

        BigDecimal avgCalories = days > 0 ? totalCalories.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgProtein = days > 0 ? totalProtein.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgFat = days > 0 ? totalFat.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgCarbs = days > 0 ? totalCarbs.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return NutritionStatsResponse.builder()
                .userId(query.getUserId())
                .days(days)
                .avgCalories(avgCalories)
                .avgProtein(avgProtein)
                .avgFat(avgFat)
                .avgCarbs(avgCarbs)
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalFat(totalFat)
                .totalCarbs(totalCarbs)
                .build();
    }

    public com.example.diet.nutrition.api.response.DailyNutritionSummaryResponse getDailySummary(GetDailySummaryQuery query) {
        Long userId = query.getUserId();
        LocalDate date = query.getDate() != null ? query.getDate() : LocalDate.now();

        NutritionGoalResponse goal = null;
        try {
            goal = userApi.getNutritionGoal(GetNutritionGoalQuery.builder().userId(userId).build());
        } catch (Exception e) {
            log.warn("action=get_daily_summary_goal_failed userId={} date={} fallback=zero_target message={}",
                    userId, date, e.getMessage());
        }

        DailyNutritionSummaryResponse summary = fetchDailySummary(userId, date);

        BigDecimal calorieTarget = goal != null ? safeDecimal(goal.getCalorieTarget()) : BigDecimal.ZERO;
        BigDecimal proteinTarget = goal != null ? safeDecimal(goal.getProteinTarget()) : BigDecimal.ZERO;
        BigDecimal carbsTarget = goal != null ? safeDecimal(goal.getCarbsTarget()) : BigDecimal.ZERO;
        BigDecimal fatTarget = goal != null ? safeDecimal(goal.getFatTarget()) : BigDecimal.ZERO;

        return com.example.diet.nutrition.api.response.DailyNutritionSummaryResponse.builder()
                .date(date)
                .calorie(summary.getTotalCalories())
                .protein(summary.getTotalProtein())
                .carbs(summary.getTotalCarbs())
                .fat(summary.getTotalFat())
                .calorieTarget(calorieTarget)
                .proteinTarget(proteinTarget)
                .carbsTarget(carbsTarget)
                .fatTarget(fatTarget)
                .caloriePercentage(calcPercentage(summary.getTotalCalories(), calorieTarget))
                .proteinPercentage(calcPercentage(summary.getTotalProtein(), proteinTarget))
                .carbsPercentage(calcPercentage(summary.getTotalCarbs(), carbsTarget))
                .fatPercentage(calcPercentage(summary.getTotalFat(), fatTarget))
                .build();
    }

    public NutritionTrendResponse getNutritionTrend(GetNutritionTrendQuery query) {
        Long userId = query.getUserId();
        LocalDate endDate = query.getEndDate() != null ? query.getEndDate() : LocalDate.now();
        LocalDate startDate = query.getStartDate();
        String type = query.getType() != null ? query.getType().toLowerCase() : "";

        if (startDate == null) {
            if ("month".equals(type)) {
                startDate = endDate.minusDays(29);
            } else {
                startDate = endDate.minusDays(6);
            }
        }

        if (startDate.isAfter(endDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        List<String> dateList = new ArrayList<>();
        List<BigDecimal> calorieList = new ArrayList<>();
        List<BigDecimal> proteinList = new ArrayList<>();
        List<BigDecimal> carbsList = new ArrayList<>();
        List<BigDecimal> fatList = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DailyNutritionSummaryResponse summary = fetchDailySummary(userId, current);
            dateList.add(current.toString());
            calorieList.add(summary.getTotalCalories());
            proteinList.add(summary.getTotalProtein());
            carbsList.add(summary.getTotalCarbs());
            fatList.add(summary.getTotalFat());
            current = current.plusDays(1);
        }

        return NutritionTrendResponse.builder()
                .dateList(dateList)
                .calorieList(calorieList)
                .proteinList(proteinList)
                .carbsList(carbsList)
                .fatList(fatList)
                .build();
    }

    public List<DailyNutritionResponse> getWeeklyTrend(GetWeeklyTrendQuery query) {
        LocalDate date = query.getDate() != null ? query.getDate() : LocalDate.now();
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<DailyNutritionResponse> result = new ArrayList<>();
        LocalDate current = weekStart;

        while (!current.isAfter(weekEnd)) {
            try {
                DailyNutritionSummaryResponse summary = dietRecordApi.getDailyNutritionSummary(
                        GetDailyNutritionSummaryQuery.builder()
                                .userId(query.getUserId())
                                .date(current)
                                .build()
                );

                result.add(DailyNutritionResponse.builder()
                        .date(current)
                        .calories(summary != null ? summary.getTotalCalories() : BigDecimal.ZERO)
                        .protein(summary != null ? summary.getTotalProtein() : BigDecimal.ZERO)
                        .fat(summary != null ? summary.getTotalFat() : BigDecimal.ZERO)
                        .carbs(summary != null ? summary.getTotalCarbs() : BigDecimal.ZERO)
                        .recordCount(summary != null ? summary.getRecordCount() : 0)
                        .build());
            } catch (Exception e) {
                log.warn("action=get_weekly_trend_daily_summary_failed userId={} date={} fallback=zero_summary message={}",
                        query.getUserId(), current, e.getMessage());
                result.add(DailyNutritionResponse.builder()
                        .date(current)
                        .calories(BigDecimal.ZERO)
                        .protein(BigDecimal.ZERO)
                        .fat(BigDecimal.ZERO)
                        .carbs(BigDecimal.ZERO)
                        .recordCount(0)
                        .build());
            }
            current = current.plusDays(1);
        }

        return result;
    }

    public List<NutritionAdviceResponse> getAdvice(GetNutritionAdviceQuery query) {
        Long userId = query.getUserId();
        LocalDate date = query.getDate() != null ? query.getDate() : LocalDate.now();

        com.example.diet.nutrition.api.response.DailyNutritionSummaryResponse dailySummary = getDailySummary(
                GetDailySummaryQuery.builder().userId(userId).date(date).build()
        );

        List<NutritionAdvicePO> adviceRules = nutritionAdviceMapper.selectList(
                new LambdaQueryWrapper<NutritionAdvicePO>()
                        .eq(NutritionAdvicePO::getStatus, 1)
        );

        if (adviceRules == null || adviceRules.isEmpty()) {
            return List.of();
        }

        List<NutritionAdviceResponse> matched = new ArrayList<>();
        for (NutritionAdvicePO rule : adviceRules) {
            if (Boolean.TRUE.equals(rule.getIsDefault())) {
                continue;
            }
            BigDecimal percentage = resolvePercentage(rule.getConditionType(), dailySummary);
            if (percentage == null) {
                continue;
            }
            if (matchesRange(percentage, rule.getMinPercentage(), rule.getMaxPercentage())) {
                matched.add(toAdviceResponse(rule));
            }
        }

        if (matched.isEmpty()) {
            for (NutritionAdvicePO rule : adviceRules) {
                if (Boolean.TRUE.equals(rule.getIsDefault())) {
                    matched.add(toAdviceResponse(rule));
                }
            }
        }

        matched.sort(Comparator.comparingInt(this::safePriority).reversed());
        return matched;
    }

    public PageResponse<NutritionAdviceResponse> listAdvice(ListNutritionAdviceQuery query) {
        int page = Math.max(query.getPage(), 1);
        int size = Math.max(query.getSize(), 1);

        LambdaQueryWrapper<NutritionAdvicePO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getConditionType())) {
            wrapper.eq(NutritionAdvicePO::getConditionType, query.getConditionType().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(NutritionAdvicePO::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(NutritionAdvicePO::getTitle, keyword)
                    .or()
                    .like(NutritionAdvicePO::getDescription, keyword));
        }

        wrapper.orderByDesc(NutritionAdvicePO::getPriority)
                .orderByDesc(NutritionAdvicePO::getUpdatedAt);

        IPage<NutritionAdvicePO> result = nutritionAdviceMapper.selectPage(new Page<>(page, size), wrapper);
        if (result.getRecords().isEmpty()) {
            return PageResponse.empty(page, size);
        }

        List<NutritionAdviceResponse> records = result.getRecords().stream()
                .map(this::toAdviceResponse)
                .toList();

        return PageResponse.of(records, result.getTotal(), page, size);
    }

    public NutritionAdviceResponse createAdvice(CreateNutritionAdviceCommand command) {
        NutritionAdvicePO po = new NutritionAdvicePO();
        po.setConditionType(command.getConditionType());
        po.setType(command.getType());
        po.setTitle(command.getTitle());
        po.setDescription(command.getDescription());
        po.setIsDefault(Boolean.TRUE.equals(command.getIsDefault()));
        po.setPriority(command.getPriority() != null ? command.getPriority() : 10);
        po.setStatus(command.getStatus() != null ? command.getStatus() : 1);

        if (!Boolean.TRUE.equals(command.getIsDefault())) {
            po.setMinPercentage(command.getMinPercentage());
            po.setMaxPercentage(command.getMaxPercentage());
        }

        nutritionAdviceMapper.insert(po);
        NutritionAdvicePO saved = nutritionAdviceMapper.selectById(po.getId());
        return toAdviceResponse(saved != null ? saved : po);
    }

    public NutritionAdviceResponse updateAdvice(UpdateNutritionAdviceCommand command) {
        if (command.getId() == null) {
            throw new BusinessException(400, "建议ID不能为空");
        }

        NutritionAdvicePO existing = nutritionAdviceMapper.selectById(command.getId());
        if (existing == null) {
            throw new BusinessException(404, "营养建议不存在");
        }

        if (StringUtils.hasText(command.getConditionType())) {
            existing.setConditionType(command.getConditionType().trim());
        }
        if (StringUtils.hasText(command.getType())) {
            existing.setType(command.getType().trim());
        }
        if (StringUtils.hasText(command.getTitle())) {
            existing.setTitle(command.getTitle().trim());
        }
        if (StringUtils.hasText(command.getDescription())) {
            existing.setDescription(command.getDescription().trim());
        }
        if (command.getPriority() != null) {
            existing.setPriority(command.getPriority());
        }
        if (command.getStatus() != null) {
            existing.setStatus(command.getStatus());
        }

        if (command.getIsDefault() != null) {
            existing.setIsDefault(command.getIsDefault());
            if (Boolean.TRUE.equals(command.getIsDefault())) {
                existing.setMinPercentage(null);
                existing.setMaxPercentage(null);
            }
        }

        if (command.getIsDefault() == null || !Boolean.TRUE.equals(command.getIsDefault())) {
            if (command.getMinPercentage() != null) {
                existing.setMinPercentage(command.getMinPercentage());
            }
            if (command.getMaxPercentage() != null) {
                existing.setMaxPercentage(command.getMaxPercentage());
            }
        }

        nutritionAdviceMapper.updateById(existing);
        NutritionAdvicePO saved = nutritionAdviceMapper.selectById(existing.getId());
        return toAdviceResponse(saved != null ? saved : existing);
    }

    public void deleteAdvice(DeleteNutritionAdviceCommand command) {
        if (command.getId() == null) {
            throw new BusinessException(400, "建议ID不能为空");
        }
        NutritionAdvicePO existing = nutritionAdviceMapper.selectById(command.getId());
        if (existing == null) {
            throw new BusinessException(404, "营养建议不存在");
        }
        nutritionAdviceMapper.deleteById(command.getId());
    }

    @Cacheable(
            value = "healthReports",
            key = "'u:' + #query.userId + ':w:' + T(com.example.diet.nutrition.support.WeekStartUtils).resolveWeekStart(#query.weekStart)"
    )
    public HealthReportResponse getHealthReport(GetHealthReportQuery query) {
        Long userId = query.getUserId();
        LocalDate weekStart = resolveWeekStart(query.getWeekStart());
        return computeHealthReport(userId, weekStart);
    }

    @CachePut(
            value = "healthReports",
            key = "'u:' + #userId + ':w:' + T(com.example.diet.nutrition.support.WeekStartUtils).resolveWeekStart(#weekStart)"
    )
    public HealthReportResponse refreshHealthReport(Long userId, LocalDate weekStart) {
        LocalDate resolvedWeekStart = resolveWeekStart(weekStart);
        return computeHealthReport(userId, resolvedWeekStart);
    }

    private HealthReportResponse computeHealthReport(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd = weekEnd.minusWeeks(1);

        NutritionGoalResponse goal = null;
        try {
            goal = userApi.getNutritionGoal(GetNutritionGoalQuery.builder().userId(userId).build());
        } catch (Exception e) {
            log.warn("action=get_health_report_goal_failed userId={} weekStart={} fallback=zero_target message={}",
                    userId, weekStart, e.getMessage());
        }

        WeekHealthMetrics thisWeekMetrics = buildWeekHealthMetrics(userId, weekStart, weekEnd, goal);
        WeekHealthMetrics lastWeekMetrics = buildWeekHealthMetrics(userId, lastWeekStart, lastWeekEnd, goal);

        return HealthReportResponse.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .totalDays(thisWeekMetrics.totalDays)
                .recordDays(thisWeekMetrics.recordDays)
                .lastWeekRecordDays(lastWeekMetrics.recordDays)
                .healthScore(thisWeekMetrics.healthScore)
                .scoreChange(thisWeekMetrics.healthScore - lastWeekMetrics.healthScore)
                .nutritionBalance(HealthReportResponse.NutritionBalance.builder()
                        .protein(thisWeekMetrics.proteinPercentage)
                        .carbs(thisWeekMetrics.carbsPercentage)
                        .fat(thisWeekMetrics.fatPercentage)
                        .build())
                 .weeklyProgress(HealthReportResponse.WeeklyProgress.builder()
                         .calorie(HealthReportResponse.NutrientProgress.builder()
                                 .lastWeek(lastWeekMetrics.calorieTotal)
                                 .thisWeek(thisWeekMetrics.calorieTotal)
                                 .build())
                        .protein(HealthReportResponse.NutrientProgress.builder()
                                .lastWeek(lastWeekMetrics.proteinTotal)
                                .thisWeek(thisWeekMetrics.proteinTotal)
                                .build())
                        .carbs(HealthReportResponse.NutrientProgress.builder()
                                .lastWeek(lastWeekMetrics.carbsTotal)
                                .thisWeek(thisWeekMetrics.carbsTotal)
                                .build())
                        .fat(HealthReportResponse.NutrientProgress.builder()
                                .lastWeek(lastWeekMetrics.fatTotal)
                                .thisWeek(thisWeekMetrics.fatTotal)
                                .build())
                         .build())
                 .build();
    }

    private LocalDate resolveWeekStart(LocalDate weekStart) {
        LocalDate baseDate = weekStart != null ? weekStart : LocalDate.now();
        return baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private WeekHealthMetrics buildWeekHealthMetrics(
            Long userId,
            LocalDate weekStart,
            LocalDate weekEnd,
            NutritionGoalResponse goal
    ) {
        int totalDays = (int) ChronoUnit.DAYS.between(weekStart, weekEnd) + 1;
        BigDecimal calorieTotal = BigDecimal.ZERO;
        BigDecimal proteinTotal = BigDecimal.ZERO;
        BigDecimal carbsTotal = BigDecimal.ZERO;
        BigDecimal fatTotal = BigDecimal.ZERO;
        BigDecimal calorieScoreTotal = BigDecimal.ZERO;
        BigDecimal proteinScoreTotal = BigDecimal.ZERO;
        BigDecimal carbsScoreTotal = BigDecimal.ZERO;
        BigDecimal fatScoreTotal = BigDecimal.ZERO;
        int recordDays = 0;

        BigDecimal dayCalorieTarget = goal != null ? safeDecimal(goal.getCalorieTarget()) : BigDecimal.ZERO;
        BigDecimal dayProteinTarget = goal != null ? safeDecimal(goal.getProteinTarget()) : BigDecimal.ZERO;
        BigDecimal dayCarbsTarget = goal != null ? safeDecimal(goal.getCarbsTarget()) : BigDecimal.ZERO;
        BigDecimal dayFatTarget = goal != null ? safeDecimal(goal.getFatTarget()) : BigDecimal.ZERO;

        LocalDate current = weekStart;
        while (!current.isAfter(weekEnd)) {
            DailyNutritionSummaryResponse summary = fetchDailySummary(userId, current);
            BigDecimal dailyCalorie = safeDecimal(summary.getTotalCalories());
            BigDecimal dailyProtein = safeDecimal(summary.getTotalProtein());
            BigDecimal dailyCarbs = safeDecimal(summary.getTotalCarbs());
            BigDecimal dailyFat = safeDecimal(summary.getTotalFat());

            calorieTotal = calorieTotal.add(dailyCalorie);
            proteinTotal = proteinTotal.add(dailyProtein);
            carbsTotal = carbsTotal.add(dailyCarbs);
            fatTotal = fatTotal.add(dailyFat);

            calorieScoreTotal = calorieScoreTotal.add(
                    calcHealthComponentScore(calcPercentage(dailyCalorie, dayCalorieTarget))
            );
            proteinScoreTotal = proteinScoreTotal.add(
                    calcHealthComponentScore(calcPercentage(dailyProtein, dayProteinTarget))
            );
            carbsScoreTotal = carbsScoreTotal.add(
                    calcHealthComponentScore(calcPercentage(dailyCarbs, dayCarbsTarget))
            );
            fatScoreTotal = fatScoreTotal.add(
                    calcHealthComponentScore(calcPercentage(dailyFat, dayFatTarget))
            );

            if (summary.getRecordCount() > 0) {
                recordDays++;
            }
            current = current.plusDays(1);
        }

        BigDecimal totalDaysDecimal = BigDecimal.valueOf(Math.max(totalDays, 1));
        BigDecimal calorieScore = calorieScoreTotal.divide(totalDaysDecimal, 2, RoundingMode.HALF_UP);
        BigDecimal proteinScore = proteinScoreTotal.divide(totalDaysDecimal, 2, RoundingMode.HALF_UP);
        BigDecimal carbsScore = carbsScoreTotal.divide(totalDaysDecimal, 2, RoundingMode.HALF_UP);
        BigDecimal fatScore = fatScoreTotal.divide(totalDaysDecimal, 2, RoundingMode.HALF_UP);

        BigDecimal healthScoreDecimal = calorieScore
                .add(proteinScore)
                .add(carbsScore)
                .add(fatScore)
                .divide(BigDecimal.valueOf(4), 0, RoundingMode.HALF_UP);
        int healthScore = healthScoreDecimal.intValue();

        return new WeekHealthMetrics(
                totalDays,
                recordDays,
                healthScore,
                calorieTotal,
                proteinTotal,
                carbsTotal,
                fatTotal,
                proteinScore,
                carbsScore,
                fatScore
        );
    }

    private NutritionAdviceResponse toAdviceResponse(NutritionAdvicePO po) {
        if (po == null) {
            return null;
        }
        return NutritionAdviceResponse.builder()
                .id(po.getId())
                .conditionType(po.getConditionType())
                .type(po.getType())
                .title(po.getTitle())
                .description(po.getDescription())
                .content(po.getDescription())
                .minPercentage(po.getMinPercentage())
                .maxPercentage(po.getMaxPercentage())
                .isDefault(Boolean.TRUE.equals(po.getIsDefault()))
                .priority(po.getPriority())
                .status(po.getStatus())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private BigDecimal resolvePercentage(
            String conditionType,
            com.example.diet.nutrition.api.response.DailyNutritionSummaryResponse summary
    ) {
        if (!StringUtils.hasText(conditionType) || summary == null) {
            return null;
        }

        switch (conditionType.toLowerCase()) {
            case "protein":
                return safeDecimal(summary.getProteinPercentage());
            case "carbs":
                return safeDecimal(summary.getCarbsPercentage());
            case "fat":
                return safeDecimal(summary.getFatPercentage());
            case "calorie":
                return safeDecimal(summary.getCaloriePercentage());
            default:
                return null;
        }
    }

    private boolean matchesRange(BigDecimal value, Integer min, Integer max) {
        if (value == null) {
            return false;
        }
        if (min != null && value.compareTo(BigDecimal.valueOf(min)) < 0) {
            return false;
        }
        if (max != null && value.compareTo(BigDecimal.valueOf(max)) > 0) {
            return false;
        }
        return true;
    }

    private int safePriority(NutritionAdviceResponse response) {
        if (response == null || response.getPriority() == null) {
            return 0;
        }
        return response.getPriority();
    }

    private DailyNutritionSummaryResponse fetchDailySummary(Long userId, LocalDate date) {
        try {
            DailyNutritionSummaryResponse summary = dietRecordApi.getDailyNutritionSummary(
                    GetDailyNutritionSummaryQuery.builder()
                            .userId(userId)
                            .date(date)
                            .build()
            );
            if (summary != null) {
                return summary;
            }
        } catch (Exception e) {
            log.warn("action=fetch_daily_summary_failed userId={} date={} fallback=zero_summary message={}",
                    userId, date, e.getMessage());
        }

        return DailyNutritionSummaryResponse.builder()
                .totalCalories(BigDecimal.ZERO)
                .totalProtein(BigDecimal.ZERO)
                .totalFat(BigDecimal.ZERO)
                .totalCarbs(BigDecimal.ZERO)
                .recordCount(0)
                .build();
    }

    private NutritionTotals sumNutritionRange(Long userId, LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return NutritionTotals.zero();
        }
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;

        LocalDate current = start;
        while (!current.isAfter(end)) {
            DailyNutritionSummaryResponse summary = fetchDailySummary(userId, current);
            calories = calories.add(safeDecimal(summary.getTotalCalories()));
            protein = protein.add(safeDecimal(summary.getTotalProtein()));
            carbs = carbs.add(safeDecimal(summary.getTotalCarbs()));
            fat = fat.add(safeDecimal(summary.getTotalFat()));
            current = current.plusDays(1);
        }

        return new NutritionTotals(calories, protein, carbs, fat);
    }

    private int averageScore(com.example.diet.nutrition.api.response.DailyNutritionSummaryResponse summary) {
        BigDecimal total = calcHealthComponentScore(summary.getCaloriePercentage())
                .add(calcHealthComponentScore(summary.getProteinPercentage()))
                .add(calcHealthComponentScore(summary.getCarbsPercentage()))
                .add(calcHealthComponentScore(summary.getFatPercentage()));
        return total.divide(BigDecimal.valueOf(4), 0, RoundingMode.HALF_UP).intValue();
    }

    private BigDecimal calcPercentage(BigDecimal value, BigDecimal target) {
        BigDecimal safeValue = safeDecimal(value);
        BigDecimal safeTarget = safeDecimal(target);
        if (safeTarget.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return safeValue.multiply(BigDecimal.valueOf(100))
                .divide(safeTarget, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal calcHealthComponentScore(BigDecimal percentage) {
        BigDecimal baseline = BigDecimal.valueOf(100);
        BigDecimal deviation = safeDecimal(percentage).subtract(baseline).abs();
        BigDecimal score = baseline.subtract(deviation);
        return score.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : score;
    }

    // ==================== 营养文章 CRUD ====================

    public PageResponse<NutritionArticleResponse> listArticles(ListNutritionArticleQuery query) {
        int page = Math.max(query.getPage(), 1);
        int size = Math.max(query.getSize(), 1);

        LambdaQueryWrapper<NutritionArticlePO> wrapper = new LambdaQueryWrapper<>();
        if (query.getStatus() != null) {
            wrapper.eq(NutritionArticlePO::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(NutritionArticlePO::getTitle, keyword)
                    .or()
                    .like(NutritionArticlePO::getSummary, keyword));
        }

        wrapper.orderByDesc(NutritionArticlePO::getSortOrder)
                .orderByDesc(NutritionArticlePO::getCreatedAt);

        IPage<NutritionArticlePO> result = nutritionArticleMapper.selectPage(new Page<>(page, size), wrapper);
        if (result.getRecords().isEmpty()) {
            return PageResponse.empty(page, size);
        }

        List<NutritionArticleResponse> records = result.getRecords().stream()
                .map(this::toArticleResponse)
                .toList();

        return PageResponse.of(records, result.getTotal(), page, size);
    }

    @Cacheable(
            value = "nutritionArticles",
            key = "'published_page_' + (#query.page < 1 ? 1 : #query.page) + '_' + (#query.size < 1 ? 1 : #query.size)"
    )
    public PageResponse<NutritionArticleResponse> listPublishedArticles(ListPublishedArticleQuery query) {
        int page = Math.max(query.getPage(), 1);
        int size = Math.max(query.getSize(), 1);

        LambdaQueryWrapper<NutritionArticlePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NutritionArticlePO::getStatus, 1); // 只查询已发布的

        wrapper.orderByDesc(NutritionArticlePO::getSortOrder)
                .orderByDesc(NutritionArticlePO::getPublishAt);

        IPage<NutritionArticlePO> result = nutritionArticleMapper.selectPage(new Page<>(page, size), wrapper);
        if (result.getRecords().isEmpty()) {
            return PageResponse.empty(page, size);
        }

        List<NutritionArticleResponse> records = result.getRecords().stream()
                .map(this::toArticleResponse)
                .toList();

        return PageResponse.of(records, result.getTotal(), page, size);
    }

    @Cacheable(
            value = "nutritionArticles",
            key = "'detail_' + #query.id",
            condition = "#query.incrementView != true"
    )
    public NutritionArticleResponse getArticleDetail(GetArticleDetailQuery query) {
        if (query.getId() == null) {
            throw new BusinessException(400, "文章ID不能为空");
        }

        NutritionArticlePO article = nutritionArticleMapper.selectById(query.getId());
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }

        // 增加浏览量
        if (Boolean.TRUE.equals(query.getIncrementView())) {
            article.setViewCount(article.getViewCount() != null ? article.getViewCount() + 1 : 1);
            nutritionArticleMapper.updateById(article);
        }

        return toArticleResponse(article);
    }

    @CacheEvict(value = "nutritionArticles", allEntries = true)
    public NutritionArticleResponse createArticle(CreateNutritionArticleCommand command) {
        validateArticleMediaReferenceIsKey(command.getCover(), "封面");
        validateArticleContentMediaReferencesAreKeys(command.getContent());

        NutritionArticlePO po = new NutritionArticlePO();
        po.setTitle(command.getTitle());
        po.setCover(command.getCover() != null ? command.getCover().trim() : null);
        po.setSummary(command.getSummary());
        po.setContent(command.getContent());
        po.setStatus(command.getStatus() != null ? command.getStatus() : 0);
        po.setSortOrder(command.getSortOrder() != null ? command.getSortOrder() : 0);
        po.setViewCount(0);

        // 如果状态为已发布且没有设置发布时间，则设置为当前时间
        if (Integer.valueOf(1).equals(po.getStatus()) && command.getPublishAt() == null) {
            po.setPublishAt(LocalDateTime.now());
        } else {
            po.setPublishAt(command.getPublishAt());
        }

        nutritionArticleMapper.insert(po);
        NutritionArticlePO saved = nutritionArticleMapper.selectById(po.getId());
        return toArticleResponse(saved != null ? saved : po);
    }

    @CacheEvict(value = "nutritionArticles", allEntries = true)
    public NutritionArticleResponse updateArticle(UpdateNutritionArticleCommand command) {
        if (command.getId() == null) {
            throw new BusinessException(400, "文章ID不能为空");
        }

        validateArticleMediaReferenceIsKey(command.getCover(), "封面");
        if (command.getContent() != null) {
            validateArticleContentMediaReferencesAreKeys(command.getContent());
        }

        NutritionArticlePO existing = nutritionArticleMapper.selectById(command.getId());
        if (existing == null) {
            throw new BusinessException(404, "文章不存在");
        }

        if (StringUtils.hasText(command.getTitle())) {
            existing.setTitle(command.getTitle().trim());
        }
        if (command.getCover() != null) {
            existing.setCover(command.getCover().trim());
        }
        if (command.getSummary() != null) {
            existing.setSummary(command.getSummary());
        }
        if (StringUtils.hasText(command.getContent())) {
            existing.setContent(command.getContent());
        }
        if (command.getSortOrder() != null) {
            existing.setSortOrder(command.getSortOrder());
        }

        // 处理状态变更
        if (command.getStatus() != null) {
            Integer oldStatus = existing.getStatus();
            existing.setStatus(command.getStatus());

            // 如果从草稿变为已发布，设置发布时间
            if (!Integer.valueOf(1).equals(oldStatus) && Integer.valueOf(1).equals(command.getStatus())) {
                if (command.getPublishAt() != null) {
                    existing.setPublishAt(command.getPublishAt());
                } else if (existing.getPublishAt() == null) {
                    existing.setPublishAt(LocalDateTime.now());
                }
            }
        }

        if (command.getPublishAt() != null) {
            existing.setPublishAt(command.getPublishAt());
        }

        nutritionArticleMapper.updateById(existing);
        NutritionArticlePO saved = nutritionArticleMapper.selectById(existing.getId());
        return toArticleResponse(saved != null ? saved : existing);
    }

    @CacheEvict(value = "nutritionArticles", allEntries = true)
    public void deleteArticle(DeleteNutritionArticleCommand command) {
        if (command.getId() == null) {
            throw new BusinessException(400, "文章ID不能为空");
        }
        NutritionArticlePO existing = nutritionArticleMapper.selectById(command.getId());
        if (existing == null) {
            throw new BusinessException(404, "文章不存在");
        }
        nutritionArticleMapper.deleteById(command.getId());
    }

    private NutritionArticleResponse toArticleResponse(NutritionArticlePO po) {
        if (po == null) {
            return null;
        }
        return NutritionArticleResponse.builder()
                .id(po.getId())
                .title(po.getTitle())
                .cover(resolveArticleMediaUrl(po.getCover()))
                .coverKey(po.getCover())
                .summary(po.getSummary())
                .content(resolveArticleContentMediaUrls(po.getContent()))
                .status(po.getStatus())
                .publishAt(po.getPublishAt())
                .viewCount(po.getViewCount())
                .sortOrder(po.getSortOrder())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private String resolveArticleMediaUrl(String value) {
        return resolveArticleMediaUrl(value, new HashMap<>());
    }

    private String resolveArticleMediaUrl(String value, Map<String, String> urlCache) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        String key = value.trim();
        validateArticleMediaReferenceIsKey(key, "文章媒体地址");

        if (urlCache.containsKey(key)) {
            return urlCache.get(key);
        }

        try {
            PresignedUrlResponse presigned = fileApi.generateDownloadUrl(
                    GenerateDownloadUrlQuery.builder()
                            .key(key)
                            .expirationMinutes(ARTICLE_MEDIA_DOWNLOAD_EXPIRE_MINUTES)
                            .build()
            );
            if (presigned == null || !StringUtils.hasText(presigned.getUrl())) {
                throw new BusinessException(500, "生成文章媒体URL失败");
            }
            urlCache.put(key, presigned.getUrl());
            return presigned.getUrl();
        } catch (Exception e) {
            log.error("action=resolve_article_media_url_failed key={} message={}", key, e.getMessage(), e);
            throw new BusinessException(500, "生成文章媒体URL失败");
        }
    }

    private String resolveArticleContentMediaUrls(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        Map<String, String> urlCache = new HashMap<>();
        return replaceMediaAttrValues(content, value -> resolveArticleMediaUrl(value, urlCache));
    }

    private void validateArticleMediaReferenceIsKey(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        String trimmed = value.trim();
        if (isHttpUrl(trimmed) || isDataOrBlobUrl(trimmed)) {
            throw new BusinessException(400, fieldName + "必须传文件key，不能传URL");
        }
    }

    private void validateArticleContentMediaReferencesAreKeys(String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        Matcher matcher = MEDIA_URL_ATTR_PATTERN.matcher(content);
        while (matcher.find()) {
            String originalValue = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
            validateArticleMediaReferenceIsKey(originalValue, "正文媒体地址");
        }
    }

    private String replaceMediaAttrValues(String content, Function<String, String> transformer) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        Matcher matcher = MEDIA_URL_ATTR_PATTERN.matcher(content);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String originalValue = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
            String transformed = transformer.apply(originalValue);
            if (transformed == null) {
                transformed = originalValue;
            }
            String quoteWrappedValue = matcher.group(3) != null
                    ? "\"" + transformed + "\""
                    : "'" + transformed + "'";
            matcher.appendReplacement(
                    buffer,
                    Matcher.quoteReplacement(matcher.group(1) + quoteWrappedValue)
            );
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private boolean isHttpUrl(String value) {
        String lower = value.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private boolean isDataOrBlobUrl(String value) {
        String lower = value.toLowerCase();
        return lower.startsWith("data:") || lower.startsWith("blob:");
    }

    private static class NutritionTotals {
        private final BigDecimal calorie;
        private final BigDecimal protein;
        private final BigDecimal carbs;
        private final BigDecimal fat;

        private NutritionTotals(BigDecimal calorie, BigDecimal protein, BigDecimal carbs, BigDecimal fat) {
            this.calorie = calorie;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
        }

        private static NutritionTotals zero() {
            return new NutritionTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    private static class WeekHealthMetrics {
        private final int totalDays;
        private final int recordDays;
        private final int healthScore;
        private final BigDecimal calorieTotal;
        private final BigDecimal proteinTotal;
        private final BigDecimal carbsTotal;
        private final BigDecimal fatTotal;
        private final BigDecimal proteinPercentage;
        private final BigDecimal carbsPercentage;
        private final BigDecimal fatPercentage;

        private WeekHealthMetrics(
                int totalDays,
                int recordDays,
                int healthScore,
                BigDecimal calorieTotal,
                BigDecimal proteinTotal,
                BigDecimal carbsTotal,
                BigDecimal fatTotal,
                BigDecimal proteinPercentage,
                BigDecimal carbsPercentage,
                BigDecimal fatPercentage
        ) {
            this.totalDays = totalDays;
            this.recordDays = recordDays;
            this.healthScore = healthScore;
            this.calorieTotal = calorieTotal;
            this.proteinTotal = proteinTotal;
            this.carbsTotal = carbsTotal;
            this.fatTotal = fatTotal;
            this.proteinPercentage = proteinPercentage;
            this.carbsPercentage = carbsPercentage;
            this.fatPercentage = fatPercentage;
        }
    }
}
