package com.example.diet.record.api.command;

import com.example.diet.shared.cqrs.Command;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 更新饮食记录命令
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDietRecordCommand implements Command {

    @NotNull(message = "记录ID不能为空")
    private Long recordId;

    private Long userId;
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime time;
    private String mealType;
    private String remark;
    private List<CreateDietRecordCommand.DietRecordFoodItem> foods;
}
