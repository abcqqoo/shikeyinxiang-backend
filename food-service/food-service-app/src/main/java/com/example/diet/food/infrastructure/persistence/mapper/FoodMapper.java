package com.example.diet.food.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.food.infrastructure.persistence.po.FoodPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食物 Mapper
 */
@Mapper
public interface FoodMapper extends BaseMapper<FoodPO> {
}
