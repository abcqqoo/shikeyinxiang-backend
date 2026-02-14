package com.example.diet.record.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.record.infrastructure.persistence.po.DietRecordPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 饮食记录 Mapper
 */
@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecordPO> {
}
