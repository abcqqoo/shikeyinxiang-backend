package com.example.diet.shared.cqrs;

import java.io.Serializable;

/**
 * 查询标记接口
 * 查询表示读操作，不改变系统状态
 * 命名规范: 动词 + 名词 + Query (如 GetUserQuery, ListUsersQuery)
 */
public interface Query extends Serializable {
}
