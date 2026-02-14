package com.example.diet.dashboard.api.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取最新饮食记录列表查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListLatestRecordsQuery implements Query {

    /**
     * 用户ID（可选，不传则查所有用户）
     */
    private Long userId;

    /**
     * 页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 20;
}
