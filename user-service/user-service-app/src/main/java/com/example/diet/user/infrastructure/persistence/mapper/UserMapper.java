package com.example.diet.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.user.infrastructure.persistence.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 MyBatis Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    UserPO selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE email = #{email}")
    UserPO selectByEmail(@Param("email") String email);

    @Select("SELECT * FROM user WHERE openid = #{openid}")
    UserPO selectByOpenid(@Param("openid") String openid);

    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM user WHERE email = #{email}")
    int countByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM user WHERE openid = #{openid}")
    int countByOpenid(@Param("openid") String openid);
}
