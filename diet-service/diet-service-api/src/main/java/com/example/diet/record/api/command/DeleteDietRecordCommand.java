package com.example.diet.record.api.command;

import com.example.diet.shared.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除饮食记录命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDietRecordCommand implements Command {

    @NotNull(message = "记录ID不能为空")
    private Long recordId;

    /**
     * 操作者用户ID (用于权限校验)
     */
    private Long operatorUserId;
}
