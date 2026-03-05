package com.example.diet.user.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 根据用户名获取用户查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUserByUsernameQuery implements Query {
    private String username;
}
