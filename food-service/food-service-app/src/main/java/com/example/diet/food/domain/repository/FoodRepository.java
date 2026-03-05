package com.example.diet.food.domain.repository;

import com.example.diet.food.domain.model.Food;
import com.example.diet.food.domain.model.FoodId;

import java.util.List;
import java.util.Optional;

/**
 * 食物仓储接口
 */
public interface FoodRepository {

    /**
     * 保存食物
     */
    Food save(Food food);

    /**
     * 根据ID查找
     */
    Optional<Food> findById(FoodId id);

    /**
     * 根据ID删除
     */
    void deleteById(FoodId id);

    /**
     * 根据分类ID查找
     */
    List<Food> findByCategoryId(Long categoryId);

    /**
     * 检查是否存在
     */
    boolean existsById(FoodId id);
}
