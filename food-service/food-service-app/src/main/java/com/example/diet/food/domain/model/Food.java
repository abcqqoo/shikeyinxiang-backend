package com.example.diet.food.domain.model;

import com.example.diet.shared.ddd.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 食物聚合根
 */
@Getter
public class Food extends AggregateRoot<FoodId> {

    private String name;
    private String measure;
    private BigDecimal grams;
    private NutritionInfo nutritionInfo;
    private Long categoryId;
    private String imageUrl;

    protected Food() {
        // for JPA/ORM
    }

    private Food(FoodId id, String name, String measure, BigDecimal grams,
                 NutritionInfo nutritionInfo, Long categoryId, String imageUrl) {
        super(id);
        this.name = name;
        this.measure = measure;
        this.grams = grams;
        this.nutritionInfo = nutritionInfo;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
    }

    /**
     * 创建食物
     */
    public static Food create(String name, String measure, BigDecimal grams,
                              NutritionInfo nutritionInfo, Long categoryId,
                              String imageUrl) {
        return new Food(null, name, measure, grams, nutritionInfo, categoryId, imageUrl);
    }

    /**
     * 重建食物 (从持久层)
     */
    public static Food reconstitute(FoodId id, String name, String measure, BigDecimal grams,
                                    NutritionInfo nutritionInfo, Long categoryId,
                                    String imageUrl) {
        return new Food(id, name, measure, grams, nutritionInfo, categoryId, imageUrl);
    }

    /**
     * 更新基本信息
     */
    public void updateInfo(String name, String measure, BigDecimal grams,
                           NutritionInfo nutritionInfo, Long categoryId) {
        if (name != null) this.name = name;
        if (measure != null) this.measure = measure;
        if (grams != null) this.grams = grams;
        if (nutritionInfo != null) this.nutritionInfo = nutritionInfo;
        if (categoryId != null) this.categoryId = categoryId;
    }

    /**
     * 更新图片
     */
    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * 分配ID (创建后由仓储分配)
     */
    public void assignId(FoodId id) {
        if (this.id == null) {
            this.id = id;
        }
    }
}
