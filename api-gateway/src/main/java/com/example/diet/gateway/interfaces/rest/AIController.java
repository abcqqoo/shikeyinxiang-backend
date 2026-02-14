package com.example.diet.gateway.interfaces.rest;

import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;
import com.example.diet.gateway.infrastructure.security.SecurityContextUtil;
import com.example.diet.food.api.FoodApi;
import com.example.diet.food.api.query.SearchFoodsQuery;
import com.example.diet.food.api.response.FoodResponse;
import com.example.diet.user.api.ai.AiRecognitionApi;
import com.example.diet.user.api.ai.command.AiRecognitionItemCommand;
import com.example.diet.user.api.ai.command.CompleteAiRecognitionTaskCommand;
import com.example.diet.user.api.ai.command.CreateAiRecognitionTaskCommand;
import com.example.diet.user.api.ai.command.FailAiRecognitionTaskCommand;
import com.example.diet.shared.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI 服务代理控制器
 * 作为 BFF 层代理转发请求到 Python AI 服务
 * 提供 JWT 鉴权保护，Python 服务只接受内部 Token 请求
 * 
 * 包含功能：
 * - 菜肴推荐 (KNN)
 * - 图像识别 (Gemini Vision)
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final RestTemplate restTemplate;
    private static final String SOURCE_DATABASE = "database";
    private static final String SOURCE_AI_ESTIMATED = "ai_estimated";

    @DubboReference
    private AiRecognitionApi aiRecognitionApi;

    @DubboReference
    private FoodApi foodApi;

    @Value("${ai.service.url:http://127.0.0.1:8000}")
    private String aiServiceUrl;

    @Value("${ai.service.token:diet-recommend-internal-token-2024}")
    private String serviceToken;

    // ==================== 菜肴推荐接口 ====================

    /**
     * 获取菜肴推荐
     * 代理转发到 Python 推荐服务
     */
    @PostMapping("/recommend/recipes")
    public ResponseEntity<Map<String, Object>> getRecipeRecommendations(
            @RequestBody Map<String, Object> request) {

        Long userId = getCurrentUserId();
        log.debug("action=recipe_recommend_request userId={}", userId);

        return proxyRequest("/recommend", request);
    }

    // ==================== 图像识别接口 ====================

    /**
     * 分析饮食图片
     * 代理转发到 Python 图像识别服务
     * 
     * @param request 包含 image_base64 和 image_type 的请求体
     */
    @PostMapping("/recognition/analyze")
    public ResponseEntity<Map<String, Object>> analyzeFoodImage(
            @RequestBody Map<String, Object> request) {

        Long userId = getCurrentUserId();
        log.info("action=food_recognition_request userId={}", userId);

        // 验证请求参数
        if (!request.containsKey("image_base64")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "data", Collections.emptyMap(),
                    "message", "缺少图片数据 (image_base64)"
            ));
        }

        Long taskId = createRecognitionTask(userId);
        if (taskId == null) {
            log.warn("action=create_ai_recognition_task_skipped userId={} reason=task_create_failed", userId);
        }
        long requestStartMs = System.currentTimeMillis();

        ResponseEntity<Map<String, Object>> response = proxyRequest("/recognition/analyze", request);
        try {
            enrichRecognitionData(response.getBody());
        } catch (Exception e) {
            log.warn("action=enrich_recognition_data_failed message={}", e.getMessage(), e);
        }

        persistRecognitionResult(taskId, response.getBody(), (int) (System.currentTimeMillis() - requestStartMs));
        return response;
    }

    /**
     * 获取支持的图片类型
     */
    @GetMapping("/recognition/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedTypes() {
        // 直接返回支持的类型，不需要调用 Python 服务
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", Map.of(
                        "supported_types", new String[]{"image/jpeg", "image/png", "image/webp"},
                        "max_size_mb", 10
                ),
                "message", "success"
        ));
    }

    // ==================== 私有方法 ====================

    /**
     * 通用代理请求方法
     */
    private ResponseEntity<Map<String, Object>> proxyRequest(String path, Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Token", serviceToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String targetUrl = aiServiceUrl + path;
            log.debug("action=proxy_ai_request path={} targetUrl={}", path, targetUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            return ResponseEntity.ok(responseBody);

        } catch (RestClientException e) {
            log.error("action=proxy_ai_request_failed path={} message={}", path, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "code", 503,
                            "data", Collections.emptyMap(),
                            "message", "AI 服务暂时不可用，请稍后重试"
                    ));
        }
    }

    private Long createRecognitionTask(Long userId) {
        try {
            return aiRecognitionApi.createTask(CreateAiRecognitionTaskCommand.builder()
                    .userId(userId)
                    .status("pending")
                    .build());
        } catch (Exception e) {
            log.error("action=create_ai_recognition_task_failed userId={}", userId, e);
            return null;
        }
    }

    private void persistRecognitionResult(Long taskId, Map<String, Object> responseBody, int fallbackProcessingTimeMs) {
        if (taskId == null) {
            log.warn("action=persist_ai_recognition_result_skipped reason=task_id_null");
            return;
        }

        try {
            if (responseBody == null) {
                markTaskFailed(taskId, null, fallbackProcessingTimeMs, "AI 服务返回空响应");
                return;
            }

            int code = toInt(responseBody.get("code"), 500);
            String message = toStringValue(responseBody.get("message"));

            Map<String, Object> data = getMapValue(responseBody.get("data"));
            int processingTimeMs = toInt(data != null ? data.get("processing_time_ms") : null, fallbackProcessingTimeMs);
            String modelName = toStringValue(data != null ? data.get("model_version") : null);

            if (code != 200) {
                markTaskFailed(taskId, modelName, processingTimeMs, message);
                return;
            }

            List<AiRecognitionItemCommand> items = parseRecognitionItems(data != null ? data.get("items") : null);
            aiRecognitionApi.completeTask(CompleteAiRecognitionTaskCommand.builder()
                    .taskId(taskId)
                    .modelName(modelName)
                    .processingTimeMs(processingTimeMs)
                    .totalItems(items.size())
                    .items(items)
                    .build());
        } catch (Exception e) {
            log.error("action=persist_ai_recognition_result_failed taskId={}", taskId, e);
        }
    }

    private void markTaskFailed(Long taskId, String modelName, int processingTimeMs, String errorMessage) {
        try {
            aiRecognitionApi.failTask(FailAiRecognitionTaskCommand.builder()
                    .taskId(taskId)
                    .modelName(modelName)
                    .processingTimeMs(processingTimeMs)
                    .errorMessage(errorMessage)
                    .build());
        } catch (Exception e) {
            log.error("action=mark_ai_recognition_task_failed taskId={}", taskId, e);
        }
    }

    private List<AiRecognitionItemCommand> parseRecognitionItems(Object itemsValue) {
        if (!(itemsValue instanceof List<?> rawItems)) {
            return Collections.emptyList();
        }

        List<AiRecognitionItemCommand> result = new ArrayList<>();
        for (Object raw : rawItems) {
            if (!(raw instanceof Map<?, ?> rawMap)) {
                continue;
            }
            Map<String, Object> item = castStringObjectMap(rawMap);

            result.add(AiRecognitionItemCommand.builder()
                    .foodName(toStringValue(item.get("name")))
                    .confidence(toBigDecimal(item.get("confidence")))
                    .calories(toBigDecimal(item.get("calories")))
                    .proteinG(toBigDecimal(item.get("protein_g")))
                    .fatG(toBigDecimal(item.get("fat_g")))
                    .carbsG(toBigDecimal(item.get("carbs_g")))
                    .estimatedGrams(toInteger(item.get("estimated_grams")))
                    .wasSelected(Boolean.FALSE)
                    .build());
        }
        return result;
    }

    private void enrichRecognitionData(Map<String, Object> responseBody) {
        if (responseBody == null) {
            return;
        }
        int code = toInt(responseBody.get("code"), 500);
        if (code != 200) {
            return;
        }
        Map<String, Object> data = getMapValue(responseBody.get("data"));
        if (data == null) {
            return;
        }
        if (!(data.get("items") instanceof List<?> rawItems)) {
            return;
        }

        Map<String, FoodResponse> matchCache = new HashMap<>();
        for (Object raw : rawItems) {
            if (!(raw instanceof Map<?, ?> rawMap)) {
                continue;
            }
            Map<String, Object> item = castStringObjectMap(rawMap);
            String source = toStringValue(item.get("source"));
            if (SOURCE_DATABASE.equals(source)) {
                if (item.get("food_id") == null && item.get("matched_food_id") != null) {
                    item.put("food_id", item.get("matched_food_id"));
                }
                continue;
            }
            if (SOURCE_AI_ESTIMATED.equals(source)) {
                item.putIfAbsent("food_id", null);
                continue;
            }

            String recognizedName = toStringValue(item.get("name"));
            if (!matchCache.containsKey(recognizedName)) {
                matchCache.put(recognizedName, matchFoodInDatabase(recognizedName));
            }
            FoodResponse matched = matchCache.get(recognizedName);
            if (matched == null) {
                item.put("source", SOURCE_AI_ESTIMATED);
                item.put("food_id", null);
                continue;
            }

            item.put("source", SOURCE_DATABASE);
            item.put("food_id", matched.getId());
            item.put("matched_food_id", matched.getId());
            item.put("matched_food_name", matched.getName());
            item.put("name", matched.getName());

            BigDecimal estimatedGrams = toBigDecimal(item.get("estimated_grams"));
            BigDecimal baseGrams = safeDecimal(matched.getGrams());
            BigDecimal ratio = BigDecimal.ONE;
            if (estimatedGrams != null && estimatedGrams.compareTo(BigDecimal.ZERO) > 0
                    && baseGrams.compareTo(BigDecimal.ZERO) > 0) {
                ratio = estimatedGrams.divide(baseGrams, 6, RoundingMode.HALF_UP);
            }

            item.put("calories", scaleNutrition(matched.getCalories(), ratio, 1));
            item.put("protein_g", scaleNutrition(matched.getProtein(), ratio, 2));
            item.put("fat_g", scaleNutrition(matched.getFat(), ratio, 2));
            item.put("carbs_g", scaleNutrition(matched.getCarbs(), ratio, 2));
        }
    }

    private FoodResponse matchFoodInDatabase(String foodName) {
        if (foodName == null || foodName.isBlank()) {
            return null;
        }

        try {
            PageResponse<FoodResponse> page = foodApi.searchFoods(SearchFoodsQuery.builder()
                    .keyword(foodName)
                    .page(1)
                    .size(10)
                    .build());
            if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
                return null;
            }
            return pickBestMatch(foodName, page.getRecords());
        } catch (Exception e) {
            log.warn("action=food_match_failed name={} message={}", foodName, e.getMessage(), e);
            return null;
        }
    }

    private FoodResponse pickBestMatch(String recognizedName, List<FoodResponse> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        String normalizedRecognized = normalizeName(recognizedName);

        for (FoodResponse candidate : candidates) {
            if (candidate != null && normalizedRecognized.equals(normalizeName(candidate.getName()))) {
                return candidate;
            }
        }
        for (FoodResponse candidate : candidates) {
            if (candidate == null || candidate.getName() == null) {
                continue;
            }
            String candidateName = normalizeName(candidate.getName());
            if (candidateName.contains(normalizedRecognized) || normalizedRecognized.contains(candidateName)) {
                return candidate;
            }
        }

        FoodResponse best = candidates.get(0);
        int bestScore = -1;
        for (FoodResponse candidate : candidates) {
            if (candidate == null || candidate.getName() == null) {
                continue;
            }
            int score = simpleSimilarityScore(normalizedRecognized, normalizeName(candidate.getName()));
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private int simpleSimilarityScore(String a, String b) {
        if (a == null || b == null) {
            return 0;
        }
        int score = 0;
        for (int i = 0; i < a.length(); i++) {
            if (b.indexOf(a.charAt(i)) >= 0) {
                score++;
            }
        }
        return score;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private Double scaleNutrition(BigDecimal base, BigDecimal ratio, int scale) {
        if (base == null) {
            return 0.0;
        }
        return base.multiply(ratio).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castStringObjectMap(Map<?, ?> rawMap) {
        return (Map<String, Object>) rawMap;
    }

    private Map<String, Object> getMapValue(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            return castStringObjectMap(mapValue);
        }
        return null;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("action=parse_integer_failed value={} message={}", value, e.getMessage());
            return null;
        }
    }

    private int toInt(Object value, int defaultValue) {
        Integer parsed = toInteger(value);
        return parsed != null ? parsed : defaultValue;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("action=parse_decimal_failed value={} message={}", value, e.getMessage());
            return null;
        }
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long getCurrentUserId() {
        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return userId;
    }
}
