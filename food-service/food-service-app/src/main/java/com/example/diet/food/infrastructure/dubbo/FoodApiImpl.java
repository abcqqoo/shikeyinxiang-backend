package com.example.diet.food.infrastructure.dubbo;

import com.example.diet.food.api.FoodApi;
import com.example.diet.food.api.command.*;
import com.example.diet.food.api.query.*;
import com.example.diet.food.api.response.*;
import com.example.diet.food.application.FoodApplicationService;
import com.example.diet.shared.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 食物 Dubbo API 实现
 */
@DubboService
@RequiredArgsConstructor
public class FoodApiImpl implements FoodApi {

    private final FoodApplicationService applicationService;

    @Override
    public FoodResponse createFood(CreateFoodCommand command) {
        return applicationService.createFood(command);
    }

    @Override
    public FoodResponse updateFood(UpdateFoodCommand command) {
        return applicationService.updateFood(command);
    }

    @Override
    public void deleteFood(DeleteFoodCommand command) {
        applicationService.deleteFood(command);
    }

    @Override
    public void updateFoodImage(UpdateFoodImageCommand command) {
        applicationService.updateFoodImage(command);
    }

    @Override
    public BatchImportResponse batchImportFoods(BatchImportFoodsCommand command) {
        return applicationService.batchImportFoods(command);
    }

    @Override
    public FoodResponse getFood(GetFoodQuery query) {
        return applicationService.getFood(query);
    }

    @Override
    public PageResponse<FoodResponse> listFoods(ListFoodsQuery query) {
        return applicationService.listFoods(query);
    }

    @Override
    public PageResponse<FoodResponse> searchFoods(SearchFoodsQuery query) {
        return applicationService.searchFoods(query);
    }

    @Override
    public FoodCategoryResponse createCategory(CreateFoodCategoryCommand command) {
        return applicationService.createCategory(command);
    }

    @Override
    public FoodCategoryResponse updateCategory(UpdateFoodCategoryCommand command) {
        return applicationService.updateCategory(command);
    }

    @Override
    public void deleteCategory(DeleteFoodCategoryCommand command) {
        applicationService.deleteCategory(command);
    }

    @Override
    public List<FoodCategoryResponse> listCategories() {
        return applicationService.listCategories();
    }

    @Override
    public FoodCategoryResponse getCategory(GetFoodCategoryQuery query) {
        return applicationService.getCategory(query);
    }
}
