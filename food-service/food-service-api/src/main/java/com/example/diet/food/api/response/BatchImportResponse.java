package com.example.diet.food.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 批量导入响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private int successCount;
    private int failCount;
    private List<String> errorMessages;
}
