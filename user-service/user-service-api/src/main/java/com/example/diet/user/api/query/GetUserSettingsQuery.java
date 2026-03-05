package com.example.diet.user.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询用户设置.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUserSettingsQuery implements Query {

    private Long userId;
}
