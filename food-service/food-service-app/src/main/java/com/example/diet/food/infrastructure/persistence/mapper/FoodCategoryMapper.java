package com.example.diet.food.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.food.infrastructure.persistence.po.FoodCategoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 食物分类 Mapper
 */
@Mapper
public interface FoodCategoryMapper extends BaseMapper<FoodCategoryPO> {

    @Select("SELECT COUNT(*) FROM food WHERE category_id = #{categoryId}")
    long countFoodsByCategoryId(Long categoryId);
}
