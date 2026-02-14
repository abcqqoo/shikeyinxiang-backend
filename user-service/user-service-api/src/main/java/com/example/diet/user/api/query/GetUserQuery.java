package com.example.diet.user.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 根据 ID 获取用户查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUserQuery implements Query {
    private Long userId;
}
