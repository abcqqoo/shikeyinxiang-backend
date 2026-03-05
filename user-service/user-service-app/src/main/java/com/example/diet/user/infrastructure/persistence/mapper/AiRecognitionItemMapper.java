package com.example.diet.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.user.infrastructure.persistence.po.AiRecognitionItemPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI食物识别结果项 MyBatis Mapper
 */
@Mapper
public interface AiRecognitionItemMapper extends BaseMapper<AiRecognitionItemPO> {
}
