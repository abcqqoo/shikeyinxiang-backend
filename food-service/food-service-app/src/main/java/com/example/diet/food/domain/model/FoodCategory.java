package com.example.diet.food.domain.model;

import com.example.diet.shared.ddd.AggregateRoot;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 食物分类实体
 */
@Getter
public class FoodCategory extends AggregateRoot<FoodCategoryId> {

    private String name;
    private String description;
    private String color;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected FoodCategory() {
    }

    private FoodCategory(FoodCategoryId id, String name, String description, 
                         String color, Integer sortOrder) {
        super(id);
        this.name = name;
        this.description = description;
        this.color = color;
        this.sortOrder = sortOrder;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static FoodCategory create(String name, String description, String color, Integer sortOrder) {
        return new FoodCategory(null, name, description, color, sortOrder);
    }

    public static FoodCategory reconstitute(FoodCategoryId id, String name, String description,
                                            String color, Integer sortOrder,
                                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        FoodCategory category = new FoodCategory(id, name, description, color, sortOrder);
        category.createdAt = createdAt;
        category.updatedAt = updatedAt;
        return category;
    }

    public void update(String name, String description, String color, Integer sortOrder) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (color != null) this.color = color;
        if (sortOrder != null) this.sortOrder = sortOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignId(FoodCategoryId id) {
        if (this.id == null) {
            this.id = id;
        }
    }
}
