package com.example.diet.food.infrastructure.persistence;

import com.example.diet.food.domain.model.*;
import com.example.diet.food.domain.repository.FoodRepository;
import com.example.diet.food.infrastructure.persistence.mapper.FoodMapper;
import com.example.diet.food.infrastructure.persistence.po.FoodPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 食物仓储实现
 */
@Repository
@RequiredArgsConstructor
public class FoodRepositoryImpl implements FoodRepository {

    private final FoodMapper foodMapper;

    @Override
    public Food save(Food food) {
        FoodPO po = toPO(food);
        if (po.getId() == null) {
            foodMapper.insert(po);
        } else {
            foodMapper.updateById(po);
        }
        food.assignId(FoodId.of(po.getId()));
        return food;
    }

    @Override
    public Optional<Food> findById(FoodId id) {
        FoodPO po = foodMapper.selectById(id.getValue());
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public void deleteById(FoodId id) {
        foodMapper.deleteById(id.getValue());
    }

    @Override
    public List<Food> findByCategoryId(Long categoryId) {
        return foodMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FoodPO>()
                        .eq(FoodPO::getCategoryId, categoryId)
        ).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsById(FoodId id) {
        return foodMapper.selectById(id.getValue()) != null;
    }

    private FoodPO toPO(Food food) {
        FoodPO po = new FoodPO();
        if (food.getId() != null) {
            po.setId(food.getId().getValue());
        }
        po.setFoodName(food.getName());
        po.setMeasure(food.getMeasure());
        po.setGrams(food.getGrams());
        po.setCategoryId(food.getCategoryId());
        po.setImageUrl(food.getImageUrl());

        if (food.getNutritionInfo() != null) {
            po.setCalories(food.getNutritionInfo().getCalories());
            po.setProtein(food.getNutritionInfo().getProtein());
            po.setFat(food.getNutritionInfo().getFat());
            po.setSatFat(food.getNutritionInfo().getSaturatedFat());
            po.setCarbs(food.getNutritionInfo().getCarbs());
            po.setFiber(food.getNutritionInfo().getFiber());
        }
        return po;
    }

    private Food toDomain(FoodPO po) {
        NutritionInfo nutrition = NutritionInfo.of(
                po.getCalories(),
                po.getProtein(),
                po.getFat(),
                po.getSatFat(),
                po.getCarbs(),
                po.getFiber()
        );
        return Food.reconstitute(
                FoodId.of(po.getId()),
                po.getFoodName(),
                po.getMeasure(),
                po.getGrams(),
                nutrition,
                po.getCategoryId(),
                po.getImageUrl()
        );
    }
}
