package com.example.diet.nutrition.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.nutrition.infrastructure.persistence.po.NutritionArticlePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 营养文章 Mapper
 */
@Mapper
public interface NutritionArticleMapper extends BaseMapper<NutritionArticlePO> {
}
