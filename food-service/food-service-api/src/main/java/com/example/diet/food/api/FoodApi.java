package com.example.diet.food.api;

import com.example.diet.food.api.command.*;
import com.example.diet.food.api.query.*;
import com.example.diet.food.api.response.*;
import com.example.diet.shared.response.PageResponse;

import java.util.List;

/**
 * 食物服务 Dubbo API
 */
public interface FoodApi {

    // ==================== 食物命令操作 ====================

    /**
     * 创建食物
     */
    FoodResponse createFood(CreateFoodCommand command);

    /**
     * 更新食物
     */
    FoodResponse updateFood(UpdateFoodCommand command);

    /**
     * 删除食物
     */
    void deleteFood(DeleteFoodCommand command);

    /**
     * 更新食物图片
     */
    void updateFoodImage(UpdateFoodImageCommand command);

    /**
     * 批量导入食物
     */
    BatchImportResponse batchImportFoods(BatchImportFoodsCommand command);

    // ==================== 食物查询操作 ====================

    /**
     * 获取食物详情
     */
    FoodResponse getFood(GetFoodQuery query);

    /**
     * 分页查询食物列表
     */
    PageResponse<FoodResponse> listFoods(ListFoodsQuery query);

    /**
     * 搜索食物
     */
    PageResponse<FoodResponse> searchFoods(SearchFoodsQuery query);

    // ==================== 分类命令操作 ====================

    /**
     * 创建分类
     */
    FoodCategoryResponse createCategory(CreateFoodCategoryCommand command);

    /**
     * 更新分类
     */
    FoodCategoryResponse updateCategory(UpdateFoodCategoryCommand command);

    /**
     * 删除分类
     */
    void deleteCategory(DeleteFoodCategoryCommand command);

    // ==================== 分类查询操作 ====================

    /**
     * 获取所有分类
     */
    List<FoodCategoryResponse> listCategories();

    /**
     * 获取分类详情
     */
    FoodCategoryResponse getCategory(GetFoodCategoryQuery query);
}
