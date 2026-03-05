package com.example.diet.record.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.record.infrastructure.persistence.po.RecommendedRecipeDietRecordPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper for recommended recipe diet records.
 */
@Mapper
public interface RecommendedRecipeDietRecordMapper extends BaseMapper<RecommendedRecipeDietRecordPO> {
}
