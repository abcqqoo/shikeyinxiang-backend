package com.example.diet.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户持久化对象
 * 对应数据库 user 表
 */
@Data
@TableName("user")
public class UserPO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String role;

    private Integer status;

    private String avatarUrl;

    private String openid;

    private LocalDateTime createTime;
}
