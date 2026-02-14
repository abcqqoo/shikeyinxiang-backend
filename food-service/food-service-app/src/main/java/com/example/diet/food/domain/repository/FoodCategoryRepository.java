package com.example.diet.food.domain.repository;

import com.example.diet.food.domain.model.FoodCategory;
import com.example.diet.food.domain.model.FoodCategoryId;

import java.util.List;
import java.util.Optional;

/**
 * 食物分类仓储接口
 */
public interface FoodCategoryRepository {

    FoodCategory save(FoodCategory category);

    Optional<FoodCategory> findById(FoodCategoryId id);

    List<FoodCategory> findAll();

    void deleteById(FoodCategoryId id);

    boolean existsById(FoodCategoryId id);

    /**
     * 统计分类下的食物数量
     */
    long countFoodsByCategoryId(FoodCategoryId id);
}
