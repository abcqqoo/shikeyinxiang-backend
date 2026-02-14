package com.example.diet.nutrition.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.nutrition.infrastructure.persistence.po.NutritionAdvicePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 营养建议 Mapper
 */
@Mapper
public interface NutritionAdviceMapper extends BaseMapper<NutritionAdvicePO> {
}
