package com.example.diet.user.api.ai.query;

import com.example.diet.shared.cqrs.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取热门识别食物查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAiRecognitionTopFoodsQuery implements Query {

    /**
     * 返回数量限制
     */
    @Builder.Default
    private Integer limit = 10;
}
