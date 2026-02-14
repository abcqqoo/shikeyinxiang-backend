package com.example.diet.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.user.infrastructure.persistence.po.AiRecognitionTaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI食物识别任务 MyBatis Mapper
 */
@Mapper
public interface AiRecognitionTaskMapper extends BaseMapper<AiRecognitionTaskPO> {
}
