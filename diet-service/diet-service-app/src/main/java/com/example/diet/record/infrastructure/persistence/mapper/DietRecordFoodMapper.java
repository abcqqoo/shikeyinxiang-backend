package com.example.diet.record.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.record.infrastructure.persistence.po.DietRecordFoodPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 饮食记录食物明细 Mapper
 */
@Mapper
public interface DietRecordFoodMapper extends BaseMapper<DietRecordFoodPO> {
}
