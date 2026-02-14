package com.example.diet.user.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证密码查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPasswordQuery implements Query {

    /**
     * 用户标识符 (用户名或邮箱)
     */
    private String identifier;

    /**
     * 密码
     */
    private String password;
}
