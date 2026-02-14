package com.example.diet.food.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.diet.file.api.FileApi;
import com.example.diet.file.api.query.GenerateDownloadUrlQuery;
import com.example.diet.file.api.response.PresignedUrlResponse;
import com.example.diet.food.api.command.*;
import com.example.diet.food.api.query.*;
import com.example.diet.food.api.response.*;
import com.example.diet.food.domain.model.*;
import com.example.diet.food.domain.repository.FoodCategoryRepository;
import com.example.diet.food.domain.repository.FoodRepository;
import com.example.diet.food.infrastructure.persistence.mapper.FoodMapper;
import com.example.diet.food.infrastructure.persistence.po.FoodPO;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 食物应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodApplicationService {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository categoryRepository;
    private final FoodMapper foodMapper;

    @DubboReference
    private FileApi fileApi;

    private static final int FOOD_IMAGE_DOWNLOAD_EXPIRE_MINUTES = 60;

    // ==================== 食物命令 ====================

    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public FoodResponse createFood(CreateFoodCommand command) {
        NutritionInfo nutrition = NutritionInfo.of(
                command.getCalories(),
                command.getProtein(),
                command.getFat(),
                command.getSaturatedFat(),
                command.getCarbs(),
                command.getFiber()
        );

        Food food = Food.create(
                command.getName(),
                command.getMeasure(),
                command.getGrams(),
                nutrition,
                command.getCategoryId(),
                command.getImageUrl()
        );

        food = foodRepository.save(food);
        FoodResponse response = toResponse(food);
        attachFoodImageUrls(List.of(response));
        return response;
    }

    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public FoodResponse updateFood(UpdateFoodCommand command) {
        if (command.getFoodId() == null) {
            throw new BusinessException(400, "食物ID不能为空");
        }
        Food food = foodRepository.findById(FoodId.of(command.getFoodId()))
                .orElseThrow(() -> new BusinessException(404, "食物不存在"));

        NutritionInfo nutrition = null;
        if (command.getCalories() != null || command.getProtein() != null ||
            command.getFat() != null || command.getCarbs() != null) {
            nutrition = NutritionInfo.of(
                    command.getCalories() != null ? command.getCalories() : food.getNutritionInfo().getCalories(),
                    command.getProtein() != null ? command.getProtein() : food.getNutritionInfo().getProtein(),
                    command.getFat() != null ? command.getFat() : food.getNutritionInfo().getFat(),
                    command.getSaturatedFat() != null ? command.getSaturatedFat() : food.getNutritionInfo().getSaturatedFat(),
                    command.getCarbs() != null ? command.getCarbs() : food.getNutritionInfo().getCarbs(),
                    command.getFiber() != null ? command.getFiber() : food.getNutritionInfo().getFiber()
            );
        }

        food.updateInfo(
                command.getName(),
                command.getMeasure(),
                command.getGrams(),
                nutrition,
                command.getCategoryId()
        );

        if (command.getImageUrl() != null) {
            food.updateImage(command.getImageUrl());
        }

        food = foodRepository.save(food);
        FoodResponse response = toResponse(food);
        attachFoodImageUrls(List.of(response));
        return response;
    }

    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public void deleteFood(DeleteFoodCommand command) {
        FoodId foodId = FoodId.of(command.getFoodId());
        if (!foodRepository.existsById(foodId)) {
            throw new BusinessException(404, "食物不存在");
        }
        foodRepository.deleteById(foodId);
    }

    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public void updateFoodImage(UpdateFoodImageCommand command) {
        Food food = foodRepository.findById(FoodId.of(command.getFoodId()))
                .orElseThrow(() -> new BusinessException(404, "食物不存在"));
        food.updateImage(command.getImageUrl());
        foodRepository.save(food);
    }

    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public BatchImportResponse batchImportFoods(BatchImportFoodsCommand command) {
        log.info("action=batch_import_foods total={}", command.getFoods() == null ? 0 : command.getFoods().size());

        List<String> errors = new ArrayList<>();
        int success = 0;
        int fail = 0;

        for (CreateFoodCommand foodCmd : command.getFoods()) {
            try {
                createFood(foodCmd);
                success++;
            } catch (BusinessException e) {
                fail++;
                log.warn("action=batch_import_food_item_failed name={} code={} message={}",
                        foodCmd.getName(), e.getCode(), e.getMessage());
                errors.add("导入 " + foodCmd.getName() + " 失败: " + e.getMessage());
            } catch (Exception e) {
                fail++;
                log.error("action=batch_import_food_item_failed name={} message={}", foodCmd.getName(), e.getMessage(), e);
                errors.add("导入 " + foodCmd.getName() + " 失败: " + e.getMessage());
            }
        }

        log.info("action=batch_import_foods_done success={} fail={}", success, fail);

        return BatchImportResponse.builder()
                .successCount(success)
                .failCount(fail)
                .errorMessages(errors)
                .build();
    }

    // ==================== 食物查询 ====================

    public FoodResponse getFood(GetFoodQuery query) {
        Food food = foodRepository.findById(FoodId.of(query.getFoodId()))
                .orElseThrow(() -> new BusinessException(404, "食物不存在"));
        FoodResponse response = toResponse(food);
        attachFoodImageUrls(List.of(response));
        return response;
    }

    @Cacheable(value = "foods", key = "'page_' + #query.page + '_' + #query.size + '_' + #query.categoryId + '_' + (#query.sortOrder == null ? 'asc' : #query.sortOrder)")
    public PageResponse<FoodResponse> listFoods(ListFoodsQuery query) {
        LambdaQueryWrapper<FoodPO> wrapper = new LambdaQueryWrapper<>();

        if (query.getCategoryId() != null) {
            wrapper.eq(FoodPO::getCategoryId, query.getCategoryId());
        }

        if ("desc".equalsIgnoreCase(query.getSortOrder())) {
            wrapper.orderByDesc(FoodPO::getId);
        } else {
            wrapper.orderByAsc(FoodPO::getId);
        }

        IPage<FoodPO> page = foodMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        List<FoodResponse> records = page.getRecords().stream()
                .map(this::poToResponse)
                .collect(Collectors.toList());

        attachFoodImageUrls(records);

        return PageResponse.of(records, page.getTotal(), query.getPage(), query.getSize());
    }

    public PageResponse<FoodResponse> searchFoods(SearchFoodsQuery query) {
        LambdaQueryWrapper<FoodPO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(FoodPO::getFoodName, query.getKeyword());
        }

        if (query.getCategoryId() != null) {
            wrapper.eq(FoodPO::getCategoryId, query.getCategoryId());
        }

        IPage<FoodPO> page = foodMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        List<FoodResponse> searchRecords = page.getRecords().stream()
                .map(this::poToResponse)
                .collect(Collectors.toList());

        attachFoodImageUrls(searchRecords);

        return PageResponse.of(searchRecords, page.getTotal(), query.getPage(), query.getSize());
    }

    // ==================== 分类命令 ====================

    @Transactional
    @CacheEvict(value = {"foodCategories", "foods"}, allEntries = true)
    public FoodCategoryResponse createCategory(CreateFoodCategoryCommand command) {
        FoodCategory category = FoodCategory.create(
                command.getName(),
                command.getDescription(),
                command.getColor(),
                command.getSortOrder()
        );
        category = categoryRepository.save(category);
        return toCategoryResponse(category);
    }

    @Transactional
    @CacheEvict(value = {"foodCategories", "foods"}, allEntries = true)
    public FoodCategoryResponse updateCategory(UpdateFoodCategoryCommand command) {
        if (command.getCategoryId() == null) {
            throw new BusinessException(400, "分类ID不能为空");
        }
        FoodCategory category = categoryRepository.findById(FoodCategoryId.of(command.getCategoryId()))
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));

        category.update(command.getName(), command.getDescription(),
                command.getColor(), command.getSortOrder());
        category = categoryRepository.save(category);
        return toCategoryResponse(category);
    }

    @Transactional
    @CacheEvict(value = {"foodCategories", "foods"}, allEntries = true)
    public void deleteCategory(DeleteFoodCategoryCommand command) {
        FoodCategoryId id = FoodCategoryId.of(command.getCategoryId());
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException(404, "分类不存在");
        }

        long count = categoryRepository.countFoodsByCategoryId(id);
        if (count > 0) {
            throw new BusinessException(400, "该分类下还有 " + count + " 个食物，无法删除");
        }

        categoryRepository.deleteById(id);
    }

    // ==================== 分类查询 ====================

    @Cacheable(value = "foodCategories", key = "'all'")
    public List<FoodCategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public FoodCategoryResponse getCategory(GetFoodCategoryQuery query) {
        FoodCategory category = categoryRepository.findById(FoodCategoryId.of(query.getCategoryId()))
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        return toCategoryResponse(category);
    }

    // ==================== 转换方法 ====================

    private FoodResponse toResponse(Food food) {
        String categoryName = null;
        if (food.getCategoryId() != null) {
            categoryName = categoryRepository.findById(FoodCategoryId.of(food.getCategoryId()))
                    .map(FoodCategory::getName)
                    .orElse(null);
        }

        return FoodResponse.builder()
                .id(food.getId().getValue())
                .name(food.getName())
                .measure(food.getMeasure())
                .grams(food.getGrams())
                .calories(food.getNutritionInfo().getCalories())
                .protein(food.getNutritionInfo().getProtein())
                .fat(food.getNutritionInfo().getFat())
                .saturatedFat(food.getNutritionInfo().getSaturatedFat())
                .carbs(food.getNutritionInfo().getCarbs())
                .fiber(food.getNutritionInfo().getFiber())
                .categoryId(food.getCategoryId())
                .categoryName(categoryName)
                .imageUrl(food.getImageUrl())
                .build();
    }

    private FoodResponse poToResponse(FoodPO po) {
        String categoryName = null;
        if (po.getCategoryId() != null) {
            categoryName = categoryRepository.findById(FoodCategoryId.of(po.getCategoryId()))
                    .map(FoodCategory::getName)
                    .orElse(null);
        }

        return FoodResponse.builder()
                .id(po.getId())
                .name(po.getFoodName())
                .measure(po.getMeasure())
                .grams(po.getGrams())
                .calories(po.getCalories())
                .protein(po.getProtein())
                .fat(po.getFat())
                .saturatedFat(po.getSatFat())
                .carbs(po.getCarbs())
                .fiber(po.getFiber())
                .categoryId(po.getCategoryId())
                .categoryName(categoryName)
                .imageUrl(po.getImageUrl())
                .build();
    }

    private FoodCategoryResponse toCategoryResponse(FoodCategory category) {
        long foodCount = categoryRepository.countFoodsByCategoryId(category.getId());
        return FoodCategoryResponse.builder()
                .id(category.getId().getValue())
                .name(category.getName())
                .description(category.getDescription())
                .color(category.getColor())
                .sortOrder(category.getSortOrder())
                .foodCount(foodCount)
                .build();
    }

    private void attachFoodImageUrls(List<FoodResponse> foods) {
        if (foods == null || foods.isEmpty()) {
            return;
        }
        for (FoodResponse food : foods) {
            if (food == null || !StringUtils.hasText(food.getImageUrl())) {
                continue;
            }
            if (isHttpUrl(food.getImageUrl())) {
                throw new BusinessException(500, "食物图片字段必须是文件key，不能是URL");
            }
            try {
                PresignedUrlResponse presigned = fileApi.generateDownloadUrl(
                        GenerateDownloadUrlQuery.builder()
                                .key(food.getImageUrl())
                                .expirationMinutes(FOOD_IMAGE_DOWNLOAD_EXPIRE_MINUTES)
                                .build()
                );
                if (presigned == null || !StringUtils.hasText(presigned.getUrl())) {
                    throw new BusinessException(500, "食物图片下载地址生成失败");
                }
                food.setImageUrl(presigned.getUrl());
            } catch (Exception e) {
                log.error("action=resolve_food_image_url_failed foodId={} message={}", food.getId(), e.getMessage(), e);
                throw new BusinessException(500, "生成食物图片URL失败");
            }
        }
    }

    private boolean isHttpUrl(String value) {
        String lower = value.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }
}
