package com.example.diet.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.user.infrastructure.persistence.po.UserBodyMetricPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户体重/腰围记录 Mapper.
 */
@Mapper
public interface UserBodyMetricMapper extends BaseMapper<UserBodyMetricPO> {

    @Select("""
            SELECT * FROM user_body_metrics
            WHERE user_id = #{userId}
              AND record_date = #{recordDate}
            LIMIT 1
            """)
    UserBodyMetricPO selectByUserIdAndRecordDate(
            @Param("userId") Long userId,
            @Param("recordDate") LocalDate recordDate
    );

    @Select("""
            SELECT * FROM user_body_metrics
            WHERE user_id = #{userId}
            ORDER BY record_date DESC, updated_at DESC
            LIMIT 1
            """)
    UserBodyMetricPO selectLatestByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT * FROM user_body_metrics
            WHERE user_id = #{userId}
              AND record_date BETWEEN #{startDate} AND #{endDate}
            ORDER BY record_date ASC, updated_at ASC
            """)
    List<UserBodyMetricPO> selectByRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
