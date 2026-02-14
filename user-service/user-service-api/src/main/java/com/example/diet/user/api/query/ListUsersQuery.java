package com.example.diet.user.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询用户列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUsersQuery implements Query {

    /**
     * 页码 (从 1 开始)
     */
    @Builder.Default
    private int page = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private int size = 10;

    // ===== 筛选条件 =====

    /**
     * 关键字 (用户名/邮箱 模糊匹配)
     */
    private String keyword;

    /**
     * 用户名 (模糊匹配)
     */
    private String username;

    /**
     * 邮箱 (模糊匹配)
     */
    private String email;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;

    /**
     * 角色: USER, ADMIN
     */
    private String role;

    // ===== 排序 =====

    /**
     * 排序字段
     */
    @Builder.Default
    private String sortBy = "createTime";

    /**
     * 排序方向: asc, desc
     */
    @Builder.Default
    private String sortOrder = "desc";
}
