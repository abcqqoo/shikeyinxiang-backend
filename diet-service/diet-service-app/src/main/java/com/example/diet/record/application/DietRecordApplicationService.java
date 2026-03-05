package com.example.diet.record.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.diet.observability.trace.TraceMdcKeys;
import com.example.diet.record.api.command.*;
import com.example.diet.record.api.query.*;
import com.example.diet.record.api.query.ListCommonMealTemplatesQuery;
import com.example.diet.record.api.response.*;
import com.example.diet.record.api.response.CommonMealTemplateResponse;
import com.example.diet.record.application.event.DietRecordChangedEvent;
import com.example.diet.record.infrastructure.persistence.dto.DailyNutritionTotalsDTO;
import com.example.diet.record.infrastructure.persistence.dto.DailyUserCountDTO;
import com.example.diet.record.infrastructure.persistence.dto.PopularFoodStatDTO;
import com.example.diet.record.infrastructure.persistence.dto.UserDailyNutritionDTO;
import com.example.diet.record.infrastructure.persistence.mapper.DietRecordFoodMapper;
import com.example.diet.record.infrastructure.persistence.mapper.DietRecordMapper;
import com.example.diet.record.infrastructure.persistence.mapper.DietRecordStatsMapper;
import com.example.diet.record.infrastructure.persistence.mapper.RecommendedRecipeDietRecordMapper;
import com.example.diet.record.infrastructure.persistence.mapper.UserNutritionGoalMapper;
import com.example.diet.record.infrastructure.persistence.po.DietRecordFoodPO;
import com.example.diet.record.infrastructure.persistence.po.DietRecordPO;
import com.example.diet.record.infrastructure.persistence.po.RecommendedRecipeDietRecordPO;
import com.example.diet.record.infrastructure.persistence.po.UserNutritionGoalPO;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 饮食记录应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietRecordApplicationService {

    private static final String SOURCE_DATABASE = "database";
    private static final String SOURCE_AI_ESTIMATED = "ai_estimated";

    private final DietRecordMapper recordMapper;
    private final DietRecordFoodMapper foodMapper;
    private final RecommendedRecipeDietRecordMapper recipeRecordMapper;
    private final DietRecordStatsMapper statsMapper;
    private final UserNutritionGoalMapper userNutritionGoalMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DietRecordResponse createRecord(CreateDietRecordCommand command) {
        log.info("action=create_diet_record userId={} date={} mealType={} foodItems={}",
                command.getUserId(), command.getDate(), command.getMealType(), sizeOf(command.getFoods()));

        if (command.getFoods() == null || command.getFoods().isEmpty()) {
            throw new BusinessException(400, "食物列表不能为空");
        }
        if (command.getTime() == null) {
            throw new BusinessException(400, "记录时间不能为空");
        }

        List<CreateDietRecordCommand.DietRecordFoodItem> regularFoods = new ArrayList<>();
        List<CreateDietRecordCommand.DietRecordFoodItem> recipeFoods = new ArrayList<>();

        for (CreateDietRecordCommand.DietRecordFoodItem item : command.getFoods()) {
            String type = normalizeFoodType(item.getType());
            if ("recipe".equals(type)) {
                recipeFoods.add(item);
            } else if ("regular".equals(type)) {
                regularFoods.add(item);
            } else {
                throw new BusinessException(400, "食物类型无效");
            }
        }

        DietRecordPO record = null;
        if (!regularFoods.isEmpty()) {
            for (CreateDietRecordCommand.DietRecordFoodItem item : regularFoods) {
                validateRegularFoodItem(item);
            }

            record = new DietRecordPO();
            record.setUserId(command.getUserId());
            record.setDate(command.getDate());
            record.setTime(command.getTime());
            record.setMealType(command.getMealType());
            record.setRemark(command.getRemark());
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());

            BigDecimal totalCalories = regularFoods.stream()
                    .map(item -> safeDecimal(item.getCalories()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            record.setTotalCalorie(totalCalories);

            recordMapper.insert(record);

            for (CreateDietRecordCommand.DietRecordFoodItem item : regularFoods) {
                DietRecordFoodPO food = toRegularFoodPO(item, record.getId());
                foodMapper.insert(food);
            }
        }

        List<RecommendedRecipeDietRecordPO> createdRecipes = new ArrayList<>();
        if (!recipeFoods.isEmpty()) {
            for (CreateDietRecordCommand.DietRecordFoodItem recipe : recipeFoods) {
                String recipeName = resolveFoodName(recipe);
                if (recipeName == null || recipeName.isBlank()) {
                    throw new BusinessException(400, "推荐菜肴名称不能为空");
                }
                if (recipe.getCalories() == null || recipe.getCalories().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException(400, "推荐菜肴热量必须大于0");
                }

                RecommendedRecipeDietRecordPO recipeRecord = new RecommendedRecipeDietRecordPO();
                recipeRecord.setUserId(command.getUserId());
                recipeRecord.setDate(command.getDate());
                recipeRecord.setTime(command.getTime());
                recipeRecord.setMealType(command.getMealType());
                recipeRecord.setRemark(command.getRemark());
                recipeRecord.setRecipeName(recipeName);
                recipeRecord.setIngredients(recipe.getIngredients());
                recipeRecord.setInstructions(recipe.getInstructions());
                recipeRecord.setCalories(recipe.getCalories());
                recipeRecord.setProtein(safeDecimal(recipe.getProtein()));
                recipeRecord.setFat(safeDecimal(recipe.getFat()));
                recipeRecord.setCarbs(safeDecimal(recipe.getCarbs()));
                recipeRecord.setCreatedAt(LocalDateTime.now());
                recipeRecord.setUpdatedAt(LocalDateTime.now());
                recipeRecordMapper.insert(recipeRecord);
                createdRecipes.add(recipeRecord);
            }
        }

        if (record != null) {
            log.info("action=create_diet_record_done userId={} recordId={} type=regular", command.getUserId(), record.getId());
            publishDietRecordChanged(command.getUserId(), null, command.getDate(), "create", record.getId());
            return getRecord(GetDietRecordQuery.builder().recordId(record.getId()).build());
        }
        if (!createdRecipes.isEmpty()) {
            log.info("action=create_diet_record_done userId={} recordId={} type=recipe", command.getUserId(), createdRecipes.get(0).getId());
            publishDietRecordChanged(command.getUserId(), null, command.getDate(), "create", createdRecipes.get(0).getId());
            return toRecipeResponse(createdRecipes.get(0));
        }
        log.warn("action=create_diet_record_empty_result userId={} date={} mealType={}",
                command.getUserId(), command.getDate(), command.getMealType());
        return null;
    }

    @Transactional
    public DietRecordResponse updateRecord(UpdateDietRecordCommand command) {
        log.info("action=update_diet_record recordId={} foodItems={}", command.getRecordId(), sizeOf(command.getFoods()));

        DietRecordPO record = recordMapper.selectById(command.getRecordId());
        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }
        LocalDate oldDate = record.getDate();

        if (command.getDate() != null) record.setDate(command.getDate());
        if (command.getTime() != null) record.setTime(command.getTime());
        if (command.getMealType() != null) record.setMealType(command.getMealType());
        if (command.getRemark() != null) record.setRemark(command.getRemark());
        record.setUpdatedAt(LocalDateTime.now());

        // 如果更新食物列表
        if (command.getFoods() != null && !command.getFoods().isEmpty()) {
            boolean hasRecipe = command.getFoods().stream()
                    .anyMatch(item -> "recipe".equals(normalizeFoodType(item.getType())));
            if (hasRecipe) {
                throw new BusinessException(400, "推荐菜肴记录不支持更新");
            }

            // 删除旧的
            foodMapper.delete(new LambdaQueryWrapper<DietRecordFoodPO>()
                    .eq(DietRecordFoodPO::getDietRecordId, command.getRecordId()));

            // 插入新的
            BigDecimal totalCalories = BigDecimal.ZERO;
            for (CreateDietRecordCommand.DietRecordFoodItem item : command.getFoods()) {
                validateRegularFoodItem(item);
                DietRecordFoodPO food = toRegularFoodPO(item, record.getId());
                foodMapper.insert(food);

                totalCalories = totalCalories.add(safeDecimal(item.getCalories()));
            }
            record.setTotalCalorie(totalCalories);
        }

        recordMapper.updateById(record);
        log.info("action=update_diet_record_done recordId={} userId={}", record.getId(), record.getUserId());
        publishDietRecordChanged(record.getUserId(), oldDate, record.getDate(), "update", record.getId());

        return getRecord(GetDietRecordQuery.builder().recordId(record.getId()).build());
    }

    @Transactional
    public void deleteRecord(DeleteDietRecordCommand command) {
        log.info("action=delete_diet_record recordId={} operatorUserId={}", command.getRecordId(), command.getOperatorUserId());

        DietRecordPO record = recordMapper.selectById(command.getRecordId());
        if (record != null) {
            // 权限校验
            if (command.getOperatorUserId() != null && !record.getUserId().equals(command.getOperatorUserId())) {
                throw new BusinessException(403, "无权删除此记录");
            }

            foodMapper.delete(new LambdaQueryWrapper<DietRecordFoodPO>()
                    .eq(DietRecordFoodPO::getDietRecordId, command.getRecordId()));

            recordMapper.deleteById(command.getRecordId());
            log.info("action=delete_diet_record_done recordId={} type=regular", command.getRecordId());
            publishDietRecordChanged(record.getUserId(), record.getDate(), null, "delete", record.getId());
            return;
        }

        RecommendedRecipeDietRecordPO recipeRecord = recipeRecordMapper.selectById(command.getRecordId());
        if (recipeRecord == null) {
            throw new BusinessException(404, "记录不存在");
        }

        if (command.getOperatorUserId() != null && !recipeRecord.getUserId().equals(command.getOperatorUserId())) {
            throw new BusinessException(403, "无权删除此记录");
        }

        recipeRecordMapper.deleteById(command.getRecordId());
        log.info("action=delete_diet_record_done recordId={} type=recipe", command.getRecordId());
        publishDietRecordChanged(recipeRecord.getUserId(), recipeRecord.getDate(), null, "delete", recipeRecord.getId());
    }

    public DietRecordResponse getRecord(GetDietRecordQuery query) {
        log.debug("action=get_diet_record recordId={}", query.getRecordId());
        DietRecordPO record = recordMapper.selectById(query.getRecordId());
        if (record != null) {
            return toResponse(record);
        }

        RecommendedRecipeDietRecordPO recipeRecord = recipeRecordMapper.selectById(query.getRecordId());
        if (recipeRecord != null) {
            return toRecipeResponse(recipeRecord);
        }

        throw new BusinessException(404, "记录不存在");
    }

    public PageResponse<DietRecordResponse> listRecords(ListDietRecordsQuery query) {
        log.debug("action=list_diet_records userId={} page={} size={} startDate={} endDate={} mealType={}",
                query.getUserId(), query.getPage(), query.getSize(), query.getStartDate(), query.getEndDate(), query.getMealType());

        List<DietRecordResponse> regularRecords = listRegularRecords(query);
        List<DietRecordResponse> recipeRecords = listRecipeRecords(query);

        if (regularRecords.isEmpty() && recipeRecords.isEmpty()) {
            return PageResponse.empty(query.getPage(), query.getSize());
        }

        List<DietRecordResponse> allRecords = new ArrayList<>(regularRecords.size() + recipeRecords.size());
        allRecords.addAll(regularRecords);
        allRecords.addAll(recipeRecords);

        allRecords.sort(Comparator
                .comparing(DietRecordResponse::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(DietRecordResponse::getTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(DietRecordResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        int total = allRecords.size();
        int page = query.getPage();
        int size = query.getSize();
        int start = Math.max(0, (page - 1) * size);
        int end = Math.min(total, start + size);

        List<DietRecordResponse> pageRecords = start >= total
                ? Collections.emptyList()
                : new ArrayList<>(allRecords.subList(start, end));

        return PageResponse.of(pageRecords, total, page, size);
    }

    public List<DietRecordResponse> getRecordsByDate(GetRecordsByDateQuery query) {
        log.debug("action=get_diet_records_by_date userId={} date={}", query.getUserId(), query.getDate());

        List<DietRecordResponse> regularRecords = recordMapper.selectList(
                        new LambdaQueryWrapper<DietRecordPO>()
                                .eq(DietRecordPO::getUserId, query.getUserId())
                                .eq(DietRecordPO::getDate, query.getDate())
                ).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        List<DietRecordResponse> recipeRecords = recipeRecordMapper.selectList(
                        new LambdaQueryWrapper<RecommendedRecipeDietRecordPO>()
                                .eq(RecommendedRecipeDietRecordPO::getUserId, query.getUserId())
                                .eq(RecommendedRecipeDietRecordPO::getDate, query.getDate())
                ).stream()
                .map(this::toRecipeResponse)
                .collect(Collectors.toList());

        List<DietRecordResponse> merged = new ArrayList<>(regularRecords.size() + recipeRecords.size());
        merged.addAll(regularRecords);
        merged.addAll(recipeRecords);

        merged.sort(Comparator
                .comparing(DietRecordResponse::getTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(DietRecordResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));

        return merged;
    }

    public List<CommonMealTemplateResponse> listCommonMealTemplates(ListCommonMealTemplatesQuery query) {
        if (query.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        int limit = normalizeTemplateLimit(query.getLimit());
        int recentDays = normalizeTemplateRecentDays(query.getRecentDays());
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(recentDays - 1L);

        LambdaQueryWrapper<DietRecordPO> wrapper = new LambdaQueryWrapper<DietRecordPO>()
                .eq(DietRecordPO::getUserId, query.getUserId())
                .ge(DietRecordPO::getDate, startDate)
                .le(DietRecordPO::getDate, endDate)
                .orderByDesc(DietRecordPO::getDate)
                .orderByDesc(DietRecordPO::getTime)
                .orderByDesc(DietRecordPO::getCreatedAt)
                .last("LIMIT 200");
        if (StringUtils.hasText(query.getMealType())) {
            wrapper.eq(DietRecordPO::getMealType, query.getMealType().trim());
        }

        List<DietRecordPO> records = recordMapper.selectList(wrapper);
        if (records == null || records.isEmpty()) {
            return List.of();
        }

        Map<String, TemplateAccumulator> grouped = new HashMap<>();
        for (DietRecordPO record : records) {
            List<DietRecordFoodPO> foods = foodMapper.selectList(
                    new LambdaQueryWrapper<DietRecordFoodPO>()
                            .eq(DietRecordFoodPO::getDietRecordId, record.getId())
                            .orderByAsc(DietRecordFoodPO::getId)
            );
            if (foods == null || foods.isEmpty()) {
                continue;
            }

            String signature = buildTemplateSignature(record.getMealType(), foods);
            TemplateAccumulator accumulator = grouped.get(signature);
            if (accumulator == null) {
                TemplateAccumulator created = new TemplateAccumulator();
                created.signature = signature;
                created.mealType = record.getMealType();
                created.useCount = 1;
                created.lastUsedDate = record.getDate();
                created.lastUsedTime = record.getTime();
                created.lastCreatedAt = record.getCreatedAt();
                created.foods = new ArrayList<>(foods);
                grouped.put(signature, created);
                continue;
            }

            accumulator.useCount += 1;
            if (isAfterRecord(record, accumulator.lastUsedDate, accumulator.lastUsedTime, accumulator.lastCreatedAt)) {
                accumulator.lastUsedDate = record.getDate();
                accumulator.lastUsedTime = record.getTime();
                accumulator.lastCreatedAt = record.getCreatedAt();
                accumulator.foods = new ArrayList<>(foods);
            }
        }

        if (grouped.isEmpty()) {
            return List.of();
        }

        return grouped.values().stream()
                .sorted(Comparator
                        .comparingInt((TemplateAccumulator item) -> item.useCount).reversed()
                        .thenComparing(TemplateAccumulator::getLastUsedDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(TemplateAccumulator::getLastUsedTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(TemplateAccumulator::getLastCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(this::toCommonMealTemplateResponse)
                .collect(Collectors.toList());
    }

    public DailyNutritionSummaryResponse getDailyNutritionSummary(GetDailyNutritionSummaryQuery query) {
        log.debug("action=get_daily_nutrition_summary userId={} date={}", query.getUserId(), query.getDate());

        List<DietRecordPO> records = recordMapper.selectList(
                new LambdaQueryWrapper<DietRecordPO>()
                        .eq(DietRecordPO::getUserId, query.getUserId())
                        .eq(DietRecordPO::getDate, query.getDate())
        );
        List<RecommendedRecipeDietRecordPO> recipeRecords = recipeRecordMapper.selectList(
                new LambdaQueryWrapper<RecommendedRecipeDietRecordPO>()
                        .eq(RecommendedRecipeDietRecordPO::getUserId, query.getUserId())
                        .eq(RecommendedRecipeDietRecordPO::getDate, query.getDate())
        );

        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;

        for (DietRecordPO record : records) {
            List<DietRecordFoodPO> foods = foodMapper.selectList(
                    new LambdaQueryWrapper<DietRecordFoodPO>()
                            .eq(DietRecordFoodPO::getDietRecordId, record.getId())
            );

            for (DietRecordFoodPO food : foods) {
                if (food.getCalories() != null) totalCalories = totalCalories.add(food.getCalories());
                if (food.getProtein() != null) totalProtein = totalProtein.add(food.getProtein());
                if (food.getFat() != null) totalFat = totalFat.add(food.getFat());
                if (food.getCarbs() != null) totalCarbs = totalCarbs.add(food.getCarbs());
            }
        }

        for (RecommendedRecipeDietRecordPO recipe : recipeRecords) {
            totalCalories = totalCalories.add(safeDecimal(recipe.getCalories()));
            totalProtein = totalProtein.add(safeDecimal(recipe.getProtein()));
            totalFat = totalFat.add(safeDecimal(recipe.getFat()));
            totalCarbs = totalCarbs.add(safeDecimal(recipe.getCarbs()));
        }

        return DailyNutritionSummaryResponse.builder()
                .userId(query.getUserId())
                .date(query.getDate())
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalFat(totalFat)
                .totalCarbs(totalCarbs)
                .recordCount(records.size() + recipeRecords.size())
                .build();
    }

    public int countRecordsByDate(java.time.LocalDate date) {
        log.debug("action=count_records_by_date date={}", date);
        Long count = recordMapper.selectCount(
                new LambdaQueryWrapper<DietRecordPO>()
                        .eq(DietRecordPO::getDate, date)
        );
        Long recipeCount = (long) countRecommendedRecipeRecordsByDate(date);
        long total = (count != null ? count : 0L) + (recipeCount != null ? recipeCount : 0L);
        return (int) total;
    }

    public int countRecommendedRecipeRecordsByDate(LocalDate date) {
        log.debug("action=count_recommended_recipe_records_by_date date={}", date);
        Long recipeCount = recipeRecordMapper.selectCount(
                new LambdaQueryWrapper<RecommendedRecipeDietRecordPO>()
                        .eq(RecommendedRecipeDietRecordPO::getDate, date)
        );
        return recipeCount == null ? 0 : recipeCount.intValue();
    }

    public NutritionTrendSummaryResponse getNutritionTrendSummary(GetNutritionTrendSummaryQuery query) {
        log.debug("action=get_nutrition_trend_summary period={}", query.getPeriod());
        String period = normalizeTrendPeriod(query.getPeriod());
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (period) {
            case "month" -> endDate.minusDays(29);
            case "year" -> endDate.minusDays(364);
            default -> endDate.minusDays(6);
        };

        Map<LocalDate, DailyNutritionTotalsDTO> totalsMap = new HashMap<>();
        for (DailyNutritionTotalsDTO item : statsMapper.selectDailyFoodTotals(startDate, endDate)) {
            mergeDailyTotals(totalsMap, item);
        }
        for (DailyNutritionTotalsDTO item : statsMapper.selectDailyRecipeTotals(startDate, endDate)) {
            mergeDailyTotals(totalsMap, item);
        }

        Map<LocalDate, Long> activeUserMap = new HashMap<>();
        for (DailyUserCountDTO item : statsMapper.selectDailyActiveUsers(startDate, endDate)) {
            if (item.getRecordDate() != null && item.getUserCount() != null) {
                activeUserMap.put(item.getRecordDate(), item.getUserCount());
            }
        }

        List<String> dateList = new ArrayList<>();
        List<BigDecimal> calorieList = new ArrayList<>();
        List<BigDecimal> proteinList = new ArrayList<>();
        List<BigDecimal> carbsList = new ArrayList<>();
        List<BigDecimal> fatList = new ArrayList<>();
        List<Long> activeUserCountList = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DailyNutritionTotalsDTO totals = totalsMap.get(date);
            long activeUsers = activeUserMap.getOrDefault(date, 0L);

            dateList.add(date.toString());
            activeUserCountList.add(activeUsers);

            BigDecimal totalCalories = totals != null ? safeDecimal(totals.getTotalCalories()) : BigDecimal.ZERO;
            BigDecimal totalProtein = totals != null ? safeDecimal(totals.getTotalProtein()) : BigDecimal.ZERO;
            BigDecimal totalCarbs = totals != null ? safeDecimal(totals.getTotalCarbs()) : BigDecimal.ZERO;
            BigDecimal totalFat = totals != null ? safeDecimal(totals.getTotalFat()) : BigDecimal.ZERO;

            calorieList.add(averagePerUser(totalCalories, activeUsers));
            proteinList.add(averagePerUser(totalProtein, activeUsers));
            carbsList.add(averagePerUser(totalCarbs, activeUsers));
            fatList.add(averagePerUser(totalFat, activeUsers));
        }

        return NutritionTrendSummaryResponse.builder()
                .dateList(dateList)
                .calorieList(calorieList)
                .proteinList(proteinList)
                .carbsList(carbsList)
                .fatList(fatList)
                .activeUserCountList(activeUserCountList)
                .build();
    }

    public NutritionComplianceRateResponse getNutritionComplianceRate(GetNutritionComplianceRateQuery query) {
        log.debug("action=get_nutrition_compliance_rate date={}", query.getDate());
        LocalDate date = query.getDate() != null ? query.getDate() : LocalDate.now();

        Map<Long, UserDailyNutritionDTO> userTotals = new HashMap<>();
        mergeUserTotals(userTotals, statsMapper.selectUserFoodTotals(date));
        mergeUserTotals(userTotals, statsMapper.selectUserRecipeTotals(date));

        if (userTotals.isEmpty()) {
            return NutritionComplianceRateResponse.builder()
                    .date(date)
                    .complianceRate(0.0)
                    .activeUserCount(0)
                    .build();
        }

        Set<Long> userIds = userTotals.keySet();
        Map<Long, UserNutritionGoalPO> goalMap = userNutritionGoalMapper.selectList(
                        new LambdaQueryWrapper<UserNutritionGoalPO>()
                                .in(UserNutritionGoalPO::getUserId, userIds)
                ).stream()
                .collect(Collectors.toMap(UserNutritionGoalPO::getUserId, goal -> goal, (a, b) -> a));

        int compliantCount = 0;
        for (Map.Entry<Long, UserDailyNutritionDTO> entry : userTotals.entrySet()) {
            Long userId = entry.getKey();
            UserDailyNutritionDTO totals = entry.getValue();
            UserNutritionGoalPO goal = goalMap.get(userId);

            BigDecimal calorieTarget = resolveTarget(goal != null ? goal.getCalorieTarget() : null,
                    new BigDecimal("2200"));
            BigDecimal proteinTarget = resolveTarget(goal != null ? goal.getProteinTarget() : null,
                    new BigDecimal("65"));
            BigDecimal carbsTarget = resolveTarget(goal != null ? goal.getCarbsTarget() : null,
                    new BigDecimal("300"));
            BigDecimal fatTarget = resolveTarget(goal != null ? goal.getFatTarget() : null,
                    new BigDecimal("70"));

            boolean calorieOk = isWithinRange(totals.getTotalCalories(), calorieTarget);
            boolean proteinOk = isWithinRange(totals.getTotalProtein(), proteinTarget);
            boolean carbsOk = isWithinRange(totals.getTotalCarbs(), carbsTarget);
            boolean fatOk = isWithinRange(totals.getTotalFat(), fatTarget);

            if (calorieOk && proteinOk && carbsOk && fatOk) {
                compliantCount++;
            }
        }

        int activeUserCount = userTotals.size();
        double complianceRate = activeUserCount == 0
                ? 0.0
                : BigDecimal.valueOf(compliantCount * 100.0)
                        .divide(BigDecimal.valueOf(activeUserCount), 2, RoundingMode.HALF_UP)
                        .doubleValue();

        return NutritionComplianceRateResponse.builder()
                .date(date)
                .complianceRate(complianceRate)
                .activeUserCount(activeUserCount)
                .build();
    }

    public List<PopularFoodStatResponse> getPopularFoodStats(GetPopularFoodStatsQuery query) {
        log.debug("action=get_popular_food_stats period={} limit={}", query.getPeriod(), query.getLimit());
        String period = normalizePopularPeriod(query.getPeriod());
        int limit = (query.getLimit() == null || query.getLimit() <= 0) ? 10 : query.getLimit();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (period) {
            case "month" -> endDate.minusDays(29);
            case "quarter" -> endDate.minusDays(89);
            default -> endDate.minusDays(6);
        };

        List<PopularFoodStatDTO> foodStats = statsMapper.selectPopularFoods(startDate, endDate, limit);
        List<PopularFoodStatDTO> recipeStats = statsMapper.selectPopularRecipes(startDate, endDate, limit);

        List<PopularFoodStatResponse> combined = new ArrayList<>();
        for (PopularFoodStatDTO item : foodStats) {
            combined.add(PopularFoodStatResponse.builder()
                    .foodId(item.getFoodId())
                    .name(item.getName())
                    .count(item.getCount())
                    .isRecipe(false)
                    .build());
        }
        for (PopularFoodStatDTO item : recipeStats) {
            combined.add(PopularFoodStatResponse.builder()
                    .foodId(item.getFoodId())
                    .name(item.getName())
                    .count(item.getCount())
                    .isRecipe(true)
                    .build());
        }

        combined.sort(Comparator.comparing(PopularFoodStatResponse::getCount,
                Comparator.nullsLast(Comparator.reverseOrder())));

        if (combined.size() > limit) {
            return new ArrayList<>(combined.subList(0, limit));
        }
        return combined;
    }

    private DietRecordResponse toResponse(DietRecordPO record) {
        List<DietRecordFoodPO> foods = foodMapper.selectList(
                new LambdaQueryWrapper<DietRecordFoodPO>()
                        .eq(DietRecordFoodPO::getDietRecordId, record.getId())
        );

        List<DietRecordFoodResponse> foodResponses = foods.stream()
                .map(f -> DietRecordFoodResponse.builder()
                        .id(f.getId())
                        .foodId(f.getFoodId())
                        .foodName(f.getFoodName())
                        .name(f.getFoodName())
                        .amount(f.getAmount())
                        .unit(f.getUnit())
                        .grams(f.getGrams())
                        .calories(f.getCalories())
                        .protein(f.getProtein())
                        .fat(f.getFat())
                        .carbs(f.getCarbs())
                        .source(f.getSource())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalCalories = foods.stream()
                .map(f -> f.getCalories() != null ? f.getCalories() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProtein = foods.stream()
                .map(f -> f.getProtein() != null ? f.getProtein() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFat = foods.stream()
                .map(f -> f.getFat() != null ? f.getFat() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCarbs = foods.stream()
                .map(f -> f.getCarbs() != null ? f.getCarbs() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DietRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .date(record.getDate())
                .time(record.getTime())
                .mealType(record.getMealType())
                .remark(record.getRemark())
                .recordType("regular")
                .totalCalories(totalCalories)
                .totalCalorie(totalCalories)
                .totalProtein(totalProtein)
                .totalFat(totalFat)
                .totalCarbs(totalCarbs)
                .foods(foodResponses)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private List<DietRecordResponse> listRegularRecords(ListDietRecordsQuery query) {
        LambdaQueryWrapper<DietRecordPO> wrapper = new LambdaQueryWrapper<>();
        if (query.getUserId() != null) {
            wrapper.eq(DietRecordPO::getUserId, query.getUserId());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(DietRecordPO::getDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(DietRecordPO::getDate, query.getEndDate());
        }
        if (query.getMealType() != null && !query.getMealType().isBlank()) {
            wrapper.eq(DietRecordPO::getMealType, query.getMealType());
        }

        wrapper.orderByDesc(DietRecordPO::getDate).orderByDesc(DietRecordPO::getTime);

        return recordMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private List<DietRecordResponse> listRecipeRecords(ListDietRecordsQuery query) {
        LambdaQueryWrapper<RecommendedRecipeDietRecordPO> wrapper = new LambdaQueryWrapper<>();
        if (query.getUserId() != null) {
            wrapper.eq(RecommendedRecipeDietRecordPO::getUserId, query.getUserId());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(RecommendedRecipeDietRecordPO::getDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(RecommendedRecipeDietRecordPO::getDate, query.getEndDate());
        }
        if (query.getMealType() != null && !query.getMealType().isBlank()) {
            wrapper.eq(RecommendedRecipeDietRecordPO::getMealType, query.getMealType());
        }

        wrapper.orderByDesc(RecommendedRecipeDietRecordPO::getDate)
                .orderByDesc(RecommendedRecipeDietRecordPO::getTime);

        return recipeRecordMapper.selectList(wrapper).stream()
                .map(this::toRecipeResponse)
                .collect(Collectors.toList());
    }

    private DietRecordResponse toRecipeResponse(RecommendedRecipeDietRecordPO record) {
        return DietRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .date(record.getDate())
                .time(record.getTime())
                .mealType(record.getMealType())
                .remark(record.getRemark())
                .recordType("recipe")
                .recipeName(record.getRecipeName())
                .ingredients(record.getIngredients())
                .instructions(record.getInstructions())
                .calories(record.getCalories())
                .protein(record.getProtein())
                .fat(record.getFat())
                .carbs(record.getCarbs())
                .totalCalories(record.getCalories())
                .totalCalorie(record.getCalories())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private String normalizeFoodType(String type) {
        if (type == null || type.isBlank()) {
            return "regular";
        }
        return type.trim().toLowerCase();
    }

    private String normalizeTrendPeriod(String period) {
        if (period == null) {
            return "week";
        }
        String value = period.trim().toLowerCase();
        return switch (value) {
            case "month", "year" -> value;
            default -> "week";
        };
    }

    private String normalizePopularPeriod(String period) {
        if (period == null) {
            return "week";
        }
        String value = period.trim().toLowerCase();
        return switch (value) {
            case "month", "quarter" -> value;
            default -> "week";
        };
    }

    private void mergeDailyTotals(Map<LocalDate, DailyNutritionTotalsDTO> totalsMap,
                                  DailyNutritionTotalsDTO item) {
        if (item == null || item.getRecordDate() == null) {
            return;
        }
        DailyNutritionTotalsDTO target = totalsMap.computeIfAbsent(item.getRecordDate(), date -> {
            DailyNutritionTotalsDTO dto = new DailyNutritionTotalsDTO();
            dto.setRecordDate(date);
            dto.setTotalCalories(BigDecimal.ZERO);
            dto.setTotalProtein(BigDecimal.ZERO);
            dto.setTotalCarbs(BigDecimal.ZERO);
            dto.setTotalFat(BigDecimal.ZERO);
            return dto;
        });
        target.setTotalCalories(target.getTotalCalories().add(safeDecimal(item.getTotalCalories())));
        target.setTotalProtein(target.getTotalProtein().add(safeDecimal(item.getTotalProtein())));
        target.setTotalCarbs(target.getTotalCarbs().add(safeDecimal(item.getTotalCarbs())));
        target.setTotalFat(target.getTotalFat().add(safeDecimal(item.getTotalFat())));
    }

    private void mergeUserTotals(Map<Long, UserDailyNutritionDTO> userTotals,
                                 List<UserDailyNutritionDTO> items) {
        if (items == null) {
            return;
        }
        for (UserDailyNutritionDTO item : items) {
            if (item == null || item.getUserId() == null) {
                continue;
            }
            UserDailyNutritionDTO target = userTotals.computeIfAbsent(item.getUserId(), userId -> {
                UserDailyNutritionDTO dto = new UserDailyNutritionDTO();
                dto.setUserId(userId);
                dto.setRecordDate(item.getRecordDate());
                dto.setTotalCalories(BigDecimal.ZERO);
                dto.setTotalProtein(BigDecimal.ZERO);
                dto.setTotalCarbs(BigDecimal.ZERO);
                dto.setTotalFat(BigDecimal.ZERO);
                return dto;
            });
            target.setTotalCalories(target.getTotalCalories().add(safeDecimal(item.getTotalCalories())));
            target.setTotalProtein(target.getTotalProtein().add(safeDecimal(item.getTotalProtein())));
            target.setTotalCarbs(target.getTotalCarbs().add(safeDecimal(item.getTotalCarbs())));
            target.setTotalFat(target.getTotalFat().add(safeDecimal(item.getTotalFat())));
        }
    }

    private BigDecimal averagePerUser(BigDecimal total, long activeUsers) {
        if (activeUsers <= 0) {
            return BigDecimal.ZERO;
        }
        return safeDecimal(total).divide(BigDecimal.valueOf(activeUsers), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveTarget(BigDecimal target, BigDecimal defaultValue) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            return defaultValue;
        }
        return target;
    }

    private boolean isWithinRange(BigDecimal actual, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal percent = safeDecimal(actual)
                .multiply(BigDecimal.valueOf(100))
                .divide(target, 2, RoundingMode.HALF_UP);
        return percent.compareTo(BigDecimal.valueOf(80)) >= 0
                && percent.compareTo(BigDecimal.valueOf(120)) <= 0;
    }

    private String resolveFoodName(CreateDietRecordCommand.DietRecordFoodItem item) {
        if (item.getFoodName() != null && !item.getFoodName().isBlank()) {
            return item.getFoodName();
        }
        return item.getName();
    }

    private void validateRegularFoodItem(CreateDietRecordCommand.DietRecordFoodItem item) {
        String source = normalizeFoodSource(item.getSource());
        String foodName = resolveFoodName(item);

        if (SOURCE_DATABASE.equals(source)) {
            if (item.getFoodId() == null || item.getFoodId() <= 0) {
                throw new BusinessException(400, "数据库食物ID不能为空");
            }
            if (item.getAmount() == null || item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "数据库食物数量必须大于0");
            }
            if (item.getUnit() == null || item.getUnit().isBlank()) {
                throw new BusinessException(400, "数据库食物单位不能为空");
            }
            if (foodName == null || foodName.isBlank()) {
                throw new BusinessException(400, "数据库食物名称不能为空");
            }
            return;
        }

        if (SOURCE_AI_ESTIMATED.equals(source)) {
            if (foodName == null || foodName.isBlank()) {
                throw new BusinessException(400, "AI估算食物名称不能为空");
            }
            if (item.getCalories() == null || item.getProtein() == null
                    || item.getFat() == null || item.getCarbs() == null) {
                throw new BusinessException(400, "AI估算食物营养数据不完整");
            }
            if (item.getAmount() != null && item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "AI估算食物数量必须大于0");
            }
            if (item.getGrams() != null && item.getGrams().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(400, "AI估算食物克数不能为负数");
            }
            return;
        }

        throw new BusinessException(400, "食物来源无效");
    }

    private DietRecordFoodPO toRegularFoodPO(CreateDietRecordCommand.DietRecordFoodItem item, Long recordId) {
        String source = normalizeFoodSource(item.getSource());
        DietRecordFoodPO food = new DietRecordFoodPO();
        food.setDietRecordId(recordId);
        food.setSource(source);
        food.setFoodId(SOURCE_DATABASE.equals(source) ? item.getFoodId() : null);
        food.setFoodName(resolveFoodName(item));
        food.setAmount(resolveAmount(item, source));
        food.setUnit(resolveUnit(item, source));
        food.setGrams(safeDecimal(item.getGrams()));
        food.setCalories(safeDecimal(item.getCalories()));
        food.setProtein(safeDecimal(item.getProtein()));
        food.setFat(safeDecimal(item.getFat()));
        food.setCarbs(safeDecimal(item.getCarbs()));
        food.setCreatedAt(LocalDateTime.now());
        return food;
    }

    private String normalizeFoodSource(String source) {
        if (source == null || source.isBlank()) {
            return SOURCE_DATABASE;
        }
        String normalized = source.trim().toLowerCase();
        if (SOURCE_DATABASE.equals(normalized) || SOURCE_AI_ESTIMATED.equals(normalized)) {
            return normalized;
        }
        return source;
    }

    private BigDecimal resolveAmount(CreateDietRecordCommand.DietRecordFoodItem item, String source) {
        if (item.getAmount() != null && item.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            return item.getAmount();
        }
        return SOURCE_AI_ESTIMATED.equals(source) ? BigDecimal.ONE : item.getAmount();
    }

    private String resolveUnit(CreateDietRecordCommand.DietRecordFoodItem item, String source) {
        if (item.getUnit() != null && !item.getUnit().isBlank()) {
            return item.getUnit();
        }
        return SOURCE_AI_ESTIMATED.equals(source) ? "份" : item.getUnit();
    }

    private void publishDietRecordChanged(
            Long userId,
            LocalDate oldDate,
            LocalDate newDate,
            String action,
            Long recordId
    ) {
        String traceId = MDC.get(TraceMdcKeys.TRACE_ID);
        eventPublisher.publishEvent(new DietRecordChangedEvent(userId, oldDate, newDate, action, recordId, traceId));
    }

    private int normalizeTemplateLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 6;
        }
        return Math.min(limit, 20);
    }

    private int normalizeTemplateRecentDays(Integer recentDays) {
        if (recentDays == null || recentDays <= 0) {
            return 30;
        }
        return Math.min(recentDays, 90);
    }

    private String buildTemplateSignature(String mealType, List<DietRecordFoodPO> foods) {
        StringBuilder builder = new StringBuilder();
        builder.append(mealType == null ? "" : mealType).append("::");
        for (DietRecordFoodPO food : foods) {
            builder.append(food.getFoodId() == null ? "null" : food.getFoodId())
                    .append('|')
                    .append(food.getFoodName() == null ? "" : food.getFoodName().trim())
                    .append('|')
                    .append(safeDecimal(food.getAmount()).setScale(2, RoundingMode.HALF_UP).toPlainString())
                    .append('|')
                    .append(food.getUnit() == null ? "" : food.getUnit().trim())
                    .append('|')
                    .append(food.getSource() == null ? "" : food.getSource().trim())
                    .append(';');
        }
        return builder.toString();
    }

    private boolean isAfterRecord(
            DietRecordPO record,
            LocalDate date,
            java.time.LocalTime time,
            LocalDateTime createdAt
    ) {
        if (record == null) {
            return false;
        }
        if (record.getDate() != null && (date == null || record.getDate().isAfter(date))) {
            return true;
        }
        if (record.getDate() != null && date != null && record.getDate().isEqual(date)) {
            if (record.getTime() != null && (time == null || record.getTime().isAfter(time))) {
                return true;
            }
            if (record.getTime() != null && time != null && record.getTime().equals(time)) {
                return record.getCreatedAt() != null
                        && (createdAt == null || record.getCreatedAt().isAfter(createdAt));
            }
        }
        return false;
    }

    private CommonMealTemplateResponse toCommonMealTemplateResponse(TemplateAccumulator accumulator) {
        List<CommonMealTemplateResponse.TemplateFood> foods = accumulator.foods.stream()
                .map(food -> CommonMealTemplateResponse.TemplateFood.builder()
                        .foodId(food.getFoodId())
                        .foodName(food.getFoodName())
                        .amount(food.getAmount())
                        .unit(food.getUnit())
                        .grams(food.getGrams())
                        .calories(food.getCalories())
                        .protein(food.getProtein())
                        .fat(food.getFat())
                        .carbs(food.getCarbs())
                        .source(food.getSource())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalCalories = accumulator.foods.stream()
                .map(item -> safeDecimal(item.getCalories()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProtein = accumulator.foods.stream()
                .map(item -> safeDecimal(item.getProtein()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFat = accumulator.foods.stream()
                .map(item -> safeDecimal(item.getFat()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCarbs = accumulator.foods.stream()
                .map(item -> safeDecimal(item.getCarbs()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CommonMealTemplateResponse.builder()
                .templateId(UUID.nameUUIDFromBytes(accumulator.signature.getBytes(StandardCharsets.UTF_8)).toString())
                .templateName(buildTemplateName(accumulator.foods))
                .mealType(accumulator.mealType)
                .useCount(accumulator.useCount)
                .lastUsedDate(accumulator.lastUsedDate)
                .lastUsedTime(accumulator.lastUsedTime)
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalFat(totalFat)
                .totalCarbs(totalCarbs)
                .foods(foods)
                .build();
    }

    private String buildTemplateName(List<DietRecordFoodPO> foods) {
        if (foods == null || foods.isEmpty()) {
            return "常用餐";
        }

        List<String> names = foods.stream()
                .map(DietRecordFoodPO::getFoodName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (names.isEmpty()) {
            return "常用餐";
        }
        if (names.size() == 1) {
            return names.get(0);
        }
        if (names.size() == 2) {
            return names.get(0) + " + " + names.get(1);
        }
        return names.get(0) + " + " + names.get(1) + " 等" + names.size() + "种";
    }

    private static class TemplateAccumulator {
        private String signature;
        private String mealType;
        private int useCount;
        private LocalDate lastUsedDate;
        private java.time.LocalTime lastUsedTime;
        private LocalDateTime lastCreatedAt;
        private List<DietRecordFoodPO> foods;

        public LocalDate getLastUsedDate() {
            return lastUsedDate;
        }

        public java.time.LocalTime getLastUsedTime() {
            return lastUsedTime;
        }

        public LocalDateTime getLastCreatedAt() {
            return lastCreatedAt;
        }
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private int sizeOf(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
