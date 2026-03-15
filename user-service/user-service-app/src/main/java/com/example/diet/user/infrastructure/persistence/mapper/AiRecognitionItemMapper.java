package com.example.diet.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.user.infrastructure.persistence.po.AiRecognitionItemPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI食物识别结果项 MyBatis Mapper
 */
@Mapper
public interface AiRecognitionItemMapper extends BaseMapper<AiRecognitionItemPO> {

    @Select("""
            SELECT COUNT(DISTINCT t.id)
            FROM ai_recognition_task t
            INNER JOIN ai_recognition_item i ON i.task_id = t.id
            WHERE t.created_at >= #{startDateTime}
              AND t.created_at < #{endDateTime}
              AND i.was_selected = 1
            """)
    Long countDistinctRecordedTasksBetween(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Select("""
            SELECT DATE(t.created_at) AS statDate, COUNT(DISTINCT t.id) AS recordedCount
            FROM ai_recognition_task t
            INNER JOIN ai_recognition_item i ON i.task_id = t.id
            WHERE t.created_at >= #{startDateTime}
              AND t.created_at < #{endDateTime}
              AND i.was_selected = 1
            GROUP BY DATE(t.created_at)
            ORDER BY DATE(t.created_at)
            """)
    List<Map<String, Object>> selectRecordedTaskTrend(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
