package com.example.diet.user.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;
import com.example.diet.user.api.ai.command.AiRecognitionItemCommand;
import com.example.diet.user.api.ai.command.CompleteAiRecognitionTaskCommand;
import com.example.diet.user.api.ai.command.ConfirmAiRecognitionTaskRecordedCommand;
import com.example.diet.user.api.ai.command.CreateAiRecognitionTaskCommand;
import com.example.diet.user.api.ai.command.FailAiRecognitionTaskCommand;
import com.example.diet.user.api.ai.query.GetAiRecognitionTopFoodsQuery;
import com.example.diet.user.api.ai.query.GetAiRecognitionTrendQuery;
import com.example.diet.user.api.ai.query.ListAiRecognitionTasksQuery;
import com.example.diet.user.api.ai.response.AiRecognitionStatsResponse;
import com.example.diet.user.api.ai.response.AiRecognitionStatusDistributionResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTaskPageResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTaskResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTopFoodResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTrendResponse;
import com.example.diet.user.infrastructure.persistence.mapper.AiRecognitionItemMapper;
import com.example.diet.user.infrastructure.persistence.mapper.AiRecognitionTaskMapper;
import com.example.diet.user.infrastructure.persistence.mapper.UserMapper;
import com.example.diet.user.infrastructure.persistence.po.AiRecognitionItemPO;
import com.example.diet.user.infrastructure.persistence.po.AiRecognitionTaskPO;
import com.example.diet.user.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 识别分析应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecognitionApplicationService {

    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("M/d");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private final AiRecognitionTaskMapper taskMapper;
    private final AiRecognitionItemMapper itemMapper;
    private final UserMapper userMapper;

    @Transactional
    public Long createTask(CreateAiRecognitionTaskCommand command) {
        if (command == null || command.getUserId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户 ID 不能为空");
        }

        AiRecognitionTaskPO task = new AiRecognitionTaskPO();
        task.setUserId(command.getUserId());
        task.setImageUrl(command.getImageUrl());
        task.setStatus(StringUtils.hasText(command.getStatus()) ? command.getStatus() : "pending");
        task.setModelName(command.getModelName());
        task.setTotalItems(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskMapper.insert(task);
        return task.getId();
    }

    @Transactional
    public void completeTask(CompleteAiRecognitionTaskCommand command) {
        if (command == null || command.getTaskId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "任务 ID 不能为空");
        }

        AiRecognitionTaskPO task = taskMapper.selectById(command.getTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "识别任务不存在");
        }

        List<AiRecognitionItemCommand> items = command.getItems() == null
                ? Collections.emptyList()
                : command.getItems();

        task.setStatus("completed");
        task.setModelName(StringUtils.hasText(command.getModelName()) ? command.getModelName() : task.getModelName());
        task.setTotalItems(command.getTotalItems() != null ? command.getTotalItems() : items.size());
        task.setProcessingTimeMs(command.getProcessingTimeMs());
        task.setErrorMessage(null);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        itemMapper.delete(new LambdaQueryWrapper<AiRecognitionItemPO>()
                .eq(AiRecognitionItemPO::getTaskId, command.getTaskId()));

        for (AiRecognitionItemCommand item : items) {
            if (item == null) {
                continue;
            }
            AiRecognitionItemPO po = new AiRecognitionItemPO();
            po.setTaskId(command.getTaskId());
            po.setFoodName(StringUtils.hasText(item.getFoodName()) ? item.getFoodName() : "未知食物");
            po.setConfidence(normalizeDecimal(item.getConfidence(), BigDecimal.ZERO));
            po.setCalories(normalizeDecimal(item.getCalories(), null));
            po.setProteinG(normalizeDecimal(item.getProteinG(), null));
            po.setFatG(normalizeDecimal(item.getFatG(), null));
            po.setCarbsG(normalizeDecimal(item.getCarbsG(), null));
            po.setEstimatedGrams(item.getEstimatedGrams());
            po.setWasSelected(item.getWasSelected() != null ? item.getWasSelected() : Boolean.FALSE);
            po.setCreatedAt(LocalDateTime.now());
            itemMapper.insert(po);
        }
    }

    @Transactional
    public void failTask(FailAiRecognitionTaskCommand command) {
        if (command == null || command.getTaskId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "任务 ID 不能为空");
        }

        AiRecognitionTaskPO task = taskMapper.selectById(command.getTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "识别任务不存在");
        }

        task.setStatus("failed");
        task.setModelName(StringUtils.hasText(command.getModelName()) ? command.getModelName() : task.getModelName());
        task.setTotalItems(0);
        task.setProcessingTimeMs(command.getProcessingTimeMs());
        task.setErrorMessage(truncate(command.getErrorMessage(), 500));
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Transactional
    public void confirmTaskRecorded(ConfirmAiRecognitionTaskRecordedCommand command) {
        if (command == null || command.getTaskId() == null || command.getUserId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "任务 ID 和用户 ID 不能为空");
        }

        AiRecognitionTaskPO task = taskMapper.selectById(command.getTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "识别任务不存在");
        }
        if (!command.getUserId().equals(task.getUserId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "无权更新该识别任务");
        }
        if (!"completed".equalsIgnoreCase(task.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "仅已完成的识别任务可确认录入");
        }

        itemMapper.update(
                null,
                new LambdaUpdateWrapper<AiRecognitionItemPO>()
                        .eq(AiRecognitionItemPO::getTaskId, command.getTaskId())
                        .set(AiRecognitionItemPO::getWasSelected, Boolean.FALSE)
        );

        List<String> selectedFoodNames = command.getSelectedFoodNames() == null
                ? Collections.emptyList()
                : command.getSelectedFoodNames().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();

        if (selectedFoodNames.isEmpty()) {
            return;
        }

        itemMapper.update(
                null,
                new LambdaUpdateWrapper<AiRecognitionItemPO>()
                        .eq(AiRecognitionItemPO::getTaskId, command.getTaskId())
                        .in(AiRecognitionItemPO::getFoodName, selectedFoodNames)
                        .set(AiRecognitionItemPO::getWasSelected, Boolean.TRUE)
        );
    }

    public AiRecognitionStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        Long todayTotal = taskMapper.selectCount(new LambdaQueryWrapper<AiRecognitionTaskPO>()
                .ge(AiRecognitionTaskPO::getCreatedAt, startOfToday)
                .lt(AiRecognitionTaskPO::getCreatedAt, startOfTomorrow));

        Long todayRecorded = itemMapper.countDistinctRecordedTasksBetween(startOfToday, startOfTomorrow);

        double recordedRate = 0.0D;
        if (todayTotal != null && todayTotal > 0 && todayRecorded != null) {
            recordedRate = roundToScale(todayRecorded * 100.0D / todayTotal, 1);
        }

        Double avgProcessingTime = getAverageCompletedProcessingSeconds();
        String topFood = getTodayTopFood(startOfToday, startOfTomorrow);

        return AiRecognitionStatsResponse.builder()
                .todayTotal(todayTotal == null ? 0L : todayTotal)
                .recordedRate(recordedRate)
                .avgProcessingTime(avgProcessingTime)
                .topFood(topFood)
                .build();
    }

    public AiRecognitionTrendResponse getTrend(GetAiRecognitionTrendQuery query) {
        String period = normalizePeriod(query != null ? query.getPeriod() : null);
        int days = resolveDays(period);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);

        QueryWrapper<AiRecognitionTaskPO> wrapper = new QueryWrapper<>();
        wrapper.select(
                        "DATE(created_at) AS statDate",
                        "COUNT(*) AS totalCount"
                )
                .ge("created_at", startDate.atStartOfDay())
                .lt("created_at", endDate.plusDays(1).atStartOfDay())
                .groupBy("DATE(created_at)")
                .orderByAsc("DATE(created_at)");

        List<Map<String, Object>> grouped = taskMapper.selectMaps(wrapper);
        Map<LocalDate, DailyStat> groupedMap = new LinkedHashMap<>();
        for (Map<String, Object> row : grouped) {
            LocalDate date = toLocalDate(getIgnoreCase(row, "statDate"));
            if (date == null) {
                continue;
            }
            groupedMap.put(date, new DailyStat(
                    toLong(getIgnoreCase(row, "totalCount")),
                    0L
            ));
        }

        List<Map<String, Object>> recordedRows = itemMapper.selectRecordedTaskTrend(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );
        for (Map<String, Object> row : recordedRows) {
            LocalDate date = toLocalDate(getIgnoreCase(row, "statDate"));
            if (date == null) {
                continue;
            }
            DailyStat currentStat = groupedMap.getOrDefault(date, DailyStat.EMPTY);
            groupedMap.put(date, new DailyStat(
                    currentStat.total(),
                    toLong(getIgnoreCase(row, "recordedCount"))
            ));
        }

        List<String> dateList = new ArrayList<>(days);
        List<Long> totalList = new ArrayList<>(days);
        List<Long> recordedList = new ArrayList<>(days);

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            DailyStat stat = groupedMap.getOrDefault(date, DailyStat.EMPTY);

            dateList.add(date.format(DATE_LABEL_FORMATTER));
            totalList.add(stat.total());
            recordedList.add(stat.recorded());
        }

        return AiRecognitionTrendResponse.builder()
                .dateList(dateList)
                .totalList(totalList)
                .recordedList(recordedList)
                .build();
    }

    public List<AiRecognitionStatusDistributionResponse> getStatusDistribution() {
        QueryWrapper<AiRecognitionTaskPO> wrapper = new QueryWrapper<>();
        wrapper.select("status", "COUNT(*) AS cnt")
                .groupBy("status");

        List<Map<String, Object>> rows = taskMapper.selectMaps(wrapper);
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("completed", 0L);
        counts.put("failed", 0L);
        counts.put("pending", 0L);
        counts.put("cancelled", 0L);

        for (Map<String, Object> row : rows) {
            String dbStatus = toStringValue(getIgnoreCase(row, "status"));
            Long count = toLong(getIgnoreCase(row, "cnt"));
            String frontendStatus = mapStatusToFrontend(dbStatus);
            counts.put(frontendStatus, counts.getOrDefault(frontendStatus, 0L) + (count == null ? 0L : count));
        }

        List<AiRecognitionStatusDistributionResponse> result = new ArrayList<>(4);
        result.add(createDistribution("识别成功", counts.get("completed"), "#10B981"));
        result.add(createDistribution("识别失败", counts.get("failed"), "#EF4444"));
        result.add(createDistribution("处理中", counts.get("pending"), "#F59E0B"));
        result.add(createDistribution("已取消", counts.get("cancelled"), "#6B7280"));
        return result;
    }

    public List<AiRecognitionTopFoodResponse> getTopFoods(GetAiRecognitionTopFoodsQuery query) {
        int limit = normalizeLimit(query != null ? query.getLimit() : null);

        QueryWrapper<AiRecognitionItemPO> wrapper = new QueryWrapper<>();
        wrapper.select("food_name AS foodName", "COUNT(*) AS cnt")
                .isNotNull("food_name")
                .groupBy("food_name")
                .orderByDesc("cnt")
                .last("LIMIT " + limit);

        List<Map<String, Object>> rows = itemMapper.selectMaps(wrapper);
        List<AiRecognitionTopFoodResponse> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            String name = toStringValue(getIgnoreCase(row, "foodName"));
            if (!StringUtils.hasText(name)) {
                continue;
            }
            result.add(AiRecognitionTopFoodResponse.builder()
                    .name(name)
                    .count(toLong(getIgnoreCase(row, "cnt")))
                    .build());
        }
        return result;
    }

    public AiRecognitionTaskPageResponse listTasks(ListAiRecognitionTasksQuery query) {
        int page = normalizePage(query != null ? query.getPage() : null);
        int pageSize = normalizePageSize(query != null ? query.getPageSize() : null);

        LambdaQueryWrapper<AiRecognitionTaskPO> wrapper = new LambdaQueryWrapper<>();
        applyStatusFilter(wrapper, query != null ? query.getStatus() : null);

        if (query != null && query.getStartDate() != null) {
            wrapper.ge(AiRecognitionTaskPO::getCreatedAt, query.getStartDate().atStartOfDay());
        }
        if (query != null && query.getEndDate() != null) {
            wrapper.lt(AiRecognitionTaskPO::getCreatedAt, query.getEndDate().plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(AiRecognitionTaskPO::getCreatedAt);

        Page<AiRecognitionTaskPO> mpPage = new Page<>(page, pageSize);
        IPage<AiRecognitionTaskPO> taskPage = taskMapper.selectPage(mpPage, wrapper);

        List<AiRecognitionTaskPO> records = taskPage.getRecords();
        if (records == null || records.isEmpty()) {
            return AiRecognitionTaskPageResponse.builder()
                    .list(Collections.emptyList())
                    .total(taskPage.getTotal())
                    .page(page)
                    .pageSize(pageSize)
                    .build();
        }

        Set<Long> userIds = records.stream()
                .map(AiRecognitionTaskPO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> usernameMap = getUsernameMap(userIds);

        List<Long> taskIds = records.stream()
                .map(AiRecognitionTaskPO::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, List<String>> recognizedFoodsMap = getRecognizedFoodsByTaskIds(taskIds);

        List<AiRecognitionTaskResponse> list = records.stream()
                .map(task -> toTaskResponse(task, usernameMap, recognizedFoodsMap))
                .toList();

        return AiRecognitionTaskPageResponse.builder()
                .list(list)
                .total(taskPage.getTotal())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    private Map<Long, String> getUsernameMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserPO::getId, UserPO::getUsername, (a, b) -> a));
    }

    private Map<Long, List<String>> getRecognizedFoodsByTaskIds(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AiRecognitionItemPO> items = itemMapper.selectList(new LambdaQueryWrapper<AiRecognitionItemPO>()
                .in(AiRecognitionItemPO::getTaskId, taskIds)
                .orderByAsc(AiRecognitionItemPO::getId));

        Map<Long, LinkedHashSet<String>> grouped = new LinkedHashMap<>();
        for (AiRecognitionItemPO item : items) {
            if (item.getTaskId() == null || !StringUtils.hasText(item.getFoodName())) {
                continue;
            }
            grouped.computeIfAbsent(item.getTaskId(), key -> new LinkedHashSet<>());
            LinkedHashSet<String> foods = grouped.get(item.getTaskId());
            if (foods.size() < 10) {
                foods.add(item.getFoodName());
            }
        }

        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, LinkedHashSet<String>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    private AiRecognitionTaskResponse toTaskResponse(
            AiRecognitionTaskPO task,
            Map<Long, String> usernameMap,
            Map<Long, List<String>> recognizedFoodsMap
    ) {
        List<String> recognizedFoods = recognizedFoodsMap.getOrDefault(task.getId(), Collections.emptyList());
        Integer itemsCount = task.getTotalItems() != null ? task.getTotalItems() : recognizedFoods.size();

        return AiRecognitionTaskResponse.builder()
                .id(task.getId())
                .username(usernameMap.get(task.getUserId()))
                .status(mapStatusToFrontend(task.getStatus()))
                .itemsCount(itemsCount)
                .processingTime(toSeconds(task.getProcessingTimeMs()))
                .recognizedFoods(recognizedFoods)
                .createdAt(task.getCreatedAt())
                .build();
    }

    private AiRecognitionStatusDistributionResponse createDistribution(String name, Long value, String color) {
        return AiRecognitionStatusDistributionResponse.builder()
                .name(name)
                .value(value == null ? 0L : value)
                .itemStyle(Map.of("color", color))
                .build();
    }

    private String getTodayTopFood(LocalDateTime startOfToday, LocalDateTime startOfTomorrow) {
        List<Object> taskIds = taskMapper.selectObjs(new QueryWrapper<AiRecognitionTaskPO>()
                .select("id")
                .ge("created_at", startOfToday)
                .lt("created_at", startOfTomorrow));

        if (taskIds == null || taskIds.isEmpty()) {
            return null;
        }

        QueryWrapper<AiRecognitionItemPO> wrapper = new QueryWrapper<>();
        wrapper.select("food_name AS foodName", "COUNT(*) AS cnt")
                .in("task_id", taskIds)
                .isNotNull("food_name")
                .groupBy("food_name")
                .orderByDesc("cnt")
                .last("LIMIT 1");

        List<Map<String, Object>> rows = itemMapper.selectMaps(wrapper);
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return toStringValue(getIgnoreCase(rows.get(0), "foodName"));
    }

    private Double getAverageCompletedProcessingSeconds() {
        QueryWrapper<AiRecognitionTaskPO> wrapper = new QueryWrapper<>();
        wrapper.select("AVG(processing_time_ms) AS avgMs")
                .eq("status", "completed")
                .isNotNull("processing_time_ms");

        List<Map<String, Object>> rows = taskMapper.selectMaps(wrapper);
        if (rows == null || rows.isEmpty()) {
            return 0.0D;
        }

        Double avgMs = toDouble(getIgnoreCase(rows.get(0), "avgMs"));
        if (avgMs == null) {
            return 0.0D;
        }
        return roundToScale(avgMs / 1000.0D, 1);
    }

    private void applyStatusFilter(LambdaQueryWrapper<AiRecognitionTaskPO> wrapper, String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }

        String normalized = status.toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "completed" -> wrapper.eq(AiRecognitionTaskPO::getStatus, "completed");
            case "failed" -> wrapper.eq(AiRecognitionTaskPO::getStatus, "failed");
            case "pending" -> wrapper.in(AiRecognitionTaskPO::getStatus, List.of("pending", "processing"));
            case "cancelled" -> wrapper.in(AiRecognitionTaskPO::getStatus, List.of("cancelled", "canceled"));
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不支持的状态筛选: " + status);
        }
    }

    private int resolveDays(String period) {
        return switch (period) {
            case "week" -> 7;
            case "year" -> 365;
            default -> 30;
        };
    }

    private String normalizePeriod(String period) {
        if (!StringUtils.hasText(period)) {
            return "month";
        }
        String normalized = period.toLowerCase(Locale.ROOT);
        if ("week".equals(normalized) || "month".equals(normalized) || "year".equals(normalized)) {
            return normalized;
        }
        throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不支持的周期: " + period);
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    private String mapStatusToFrontend(String dbStatus) {
        if (!StringUtils.hasText(dbStatus)) {
            return "pending";
        }

        return switch (dbStatus.toLowerCase(Locale.ROOT)) {
            case "completed" -> "completed";
            case "failed" -> "failed";
            case "pending", "processing" -> "pending";
            case "cancelled", "canceled" -> "cancelled";
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "未知任务状态: " + dbStatus);
        };
    }

    private Double toSeconds(Integer ms) {
        if (ms == null || ms < 0) {
            return null;
        }
        return roundToScale(ms / 1000.0D, 3);
    }

    private double roundToScale(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private Object getIgnoreCase(Map<String, Object> map, String key) {
        if (map == null || map.isEmpty() || !StringUtils.hasText(key)) {
            return null;
        }

        if (map.containsKey(key)) {
            return map.get(key);
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        String str = value.toString().trim();
        if (!StringUtils.hasText(str)) {
            return null;
        }
        String normalized = str.length() >= 10 ? str.substring(0, 10) : str;
        if (!DATE_PATTERN.matcher(normalized).matches()) {
            return null;
        }
        return LocalDate.parse(normalized);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.debug("无法将值转换为 Long: {}", value, e);
            return 0L;
        }
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.debug("无法将值转换为 Double: {}", value, e);
            return null;
        }
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private BigDecimal normalizeDecimal(BigDecimal value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record DailyStat(Long total, Long recorded) {
        private static final DailyStat EMPTY = new DailyStat(0L, 0L);
    }
}
