package com.example.diet.gateway.interfaces.rest;

import com.example.diet.food.api.FoodApi;
import com.example.diet.food.api.command.*;
import com.example.diet.food.api.query.*;
import com.example.diet.food.api.response.*;
import com.example.diet.file.api.FileApi;
import com.example.diet.file.api.command.GenerateUploadUrlCommand;
import com.example.diet.file.api.response.PresignedUrlResponse;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.shared.response.PageResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 食物管理控制器
 * 代理 food-service 的 Dubbo API
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class FoodController {

    @DubboReference
    private FoodApi foodApi;

    @DubboReference
    private FileApi fileApi;

    // ==================== 食物操作 ====================

    /**
     * 获取食物详情
     */
    @GetMapping("/foods/{id}")
    public ResponseEntity<ApiResponse<FoodResponse>> getFood(@PathVariable Long id) {
        FoodResponse food = foodApi.getFood(GetFoodQuery.builder().foodId(id).build());
        return ResponseEntity.ok(ApiResponse.success(food));
    }

    /**
     * 分页查询食物列表
     */
    @GetMapping("/foods")
    public ResponseEntity<ApiResponse<PageResponse<FoodResponse>>> listFoods(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        ListFoodsQuery query = ListFoodsQuery.builder()
                .categoryId(categoryId)
                .page(page)
                .size(size)
                .build();

        PageResponse<FoodResponse> result = foodApi.listFoods(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 搜索食物
     */
    @GetMapping("/foods/search")
    public ResponseEntity<ApiResponse<PageResponse<FoodResponse>>> searchFoods(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        SearchFoodsQuery query = SearchFoodsQuery.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .page(page)
                .size(size)
                .build();

        PageResponse<FoodResponse> result = foodApi.searchFoods(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/food-categories")
    public ResponseEntity<ApiResponse<List<FoodCategoryResponse>>> listCategories() {
        List<FoodCategoryResponse> categories = foodApi.listCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/food-categories/{id}")
    public ResponseEntity<ApiResponse<FoodCategoryResponse>> getCategory(@PathVariable Long id) {
        FoodCategoryResponse category = foodApi.getCategory(
                GetFoodCategoryQuery.builder().categoryId(id).build());
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    // ==================== 管理员操作 ====================

    /**
     * 创建食物 (管理员)
     */
    @PostMapping("/admin/foods")
    public ResponseEntity<ApiResponse<FoodResponse>> createFood(
            @Valid @RequestBody CreateFoodCommand command) {
        FoodResponse food = foodApi.createFood(command);
        return ResponseEntity.ok(ApiResponse.success(food));
    }

    /**
     * 更新食物 (管理员)
     */
    @PutMapping("/admin/foods/{id}")
    public ResponseEntity<ApiResponse<FoodResponse>> updateFood(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFoodCommand command) {
        // 确保 ID 一致
        command = command.toBuilder().foodId(id).build();
        FoodResponse food = foodApi.updateFood(command);
        return ResponseEntity.ok(ApiResponse.success(food));
    }

    /**
     * 删除食物 (管理员)
     */
    @DeleteMapping("/admin/foods/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFood(@PathVariable Long id) {
        foodApi.deleteFood(DeleteFoodCommand.builder().foodId(id).build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 更新食物图片 (管理员)
     */
    @PutMapping("/admin/foods/{id}/image")
    public ResponseEntity<ApiResponse<Void>> updateFoodImage(
            @PathVariable Long id,
            @RequestBody UpdateFoodImageCommand command) {
        command = command.toBuilder().foodId(id).build();
        foodApi.updateFoodImage(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 生成食物图片上传URL (管理员)
     */
    @PostMapping("/admin/foods/{id}/image/upload-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> generateFoodImageUploadUrl(
            @PathVariable Long id,
            @Valid @RequestBody GenerateFoodImageUploadUrlRequest request) {
        String extension = resolveExtension(request.getContentType());
        String fileName = java.util.UUID.randomUUID().toString().replace("-", "");
        String key = extension == null
                ? String.format("food-image/%d/%s", id, fileName)
                : String.format("food-image/%d/%s.%s", id, fileName, extension);

        GenerateUploadUrlCommand command = GenerateUploadUrlCommand.builder()
                .key(key)
                .contentType(request.getContentType())
                .expirationMinutes(request.getExpirationMinutes() != null ? request.getExpirationMinutes() : 15)
                .build();

        PresignedUrlResponse response = fileApi.generateUploadUrl(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 批量导入食物 (管理员)
     */
    @PostMapping("/admin/foods/batch-import")
    public ResponseEntity<ApiResponse<BatchImportResponse>> batchImportFoods(
            @Valid @RequestBody BatchImportFoodsCommand command) {
        BatchImportResponse result = foodApi.batchImportFoods(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 创建分类 (管理员)
     */
    @PostMapping("/admin/food-categories")
    public ResponseEntity<ApiResponse<FoodCategoryResponse>> createCategory(
            @Valid @RequestBody CreateFoodCategoryCommand command) {
        FoodCategoryResponse category = foodApi.createCategory(command);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    /**
     * 更新分类 (管理员)
     */
    @PutMapping("/admin/food-categories/{id}")
    public ResponseEntity<ApiResponse<FoodCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFoodCategoryCommand command) {
        command = command.toBuilder().categoryId(id).build();
        FoodCategoryResponse category = foodApi.updateCategory(command);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    /**
     * 删除分类 (管理员)
     */
    @DeleteMapping("/admin/food-categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        foodApi.deleteCategory(DeleteFoodCategoryCommand.builder().categoryId(id).build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String resolveExtension(String contentType) {
        if (contentType == null) {
            return null;
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            case "application/pdf" -> "pdf";
            default -> null;
        };
    }

    @Data
    public static class GenerateFoodImageUploadUrlRequest {
        private String contentType;
        private Integer expirationMinutes;
    }
}
