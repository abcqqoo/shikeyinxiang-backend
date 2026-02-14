package com.example.diet.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.user.infrastructure.persistence.po.FeedbackPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈 MyBatis Mapper
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<FeedbackPO> {
}
