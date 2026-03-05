package com.example.diet.record.api.query;

import com.example.diet.shared.cqrs.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取饮食记录查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDietRecordQuery implements Query {

    @NotNull(message = "记录ID不能为空")
    private Long recordId;
}
