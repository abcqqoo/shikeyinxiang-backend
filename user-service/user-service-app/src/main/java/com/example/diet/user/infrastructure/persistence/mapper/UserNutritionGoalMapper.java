package com.example.diet.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.user.infrastructure.persistence.po.UserNutritionGoalPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户营养目标 MyBatis Mapper
 */
@Mapper
public interface UserNutritionGoalMapper extends BaseMapper<UserNutritionGoalPO> {

    @Select("SELECT * FROM user_nutrition_goals WHERE user_id = #{userId} LIMIT 1")
    UserNutritionGoalPO selectByUserId(@Param("userId") Long userId);
}
