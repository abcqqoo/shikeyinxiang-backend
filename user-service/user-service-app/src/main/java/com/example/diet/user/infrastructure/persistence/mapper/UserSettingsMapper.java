package com.example.diet.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.user.infrastructure.persistence.po.UserSettingsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户设置 Mapper.
 */
@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettingsPO> {

    @Select("""
            SELECT * FROM user_settings
            WHERE user_id = #{userId}
            LIMIT 1
            """)
    UserSettingsPO selectByUserId(@Param("userId") Long userId);
}
