package com.example.diet.record.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.record.infrastructure.persistence.po.UserNutritionGoalPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户营养目标 Mapper
 */
@Mapper
public interface UserNutritionGoalMapper extends BaseMapper<UserNutritionGoalPO> {
}
