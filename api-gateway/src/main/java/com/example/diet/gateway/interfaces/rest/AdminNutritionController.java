package com.example.diet.gateway.interfaces.rest;

import com.example.diet.nutrition.api.NutritionApi;
import com.example.diet.nutrition.api.command.CreateNutritionAdviceCommand;
import com.example.diet.nutrition.api.command.DeleteNutritionAdviceCommand;
import com.example.diet.nutrition.api.command.UpdateNutritionAdviceCommand;
import com.example.diet.nutrition.api.query.ListNutritionAdviceQuery;
import com.example.diet.nutrition.api.response.NutritionAdviceResponse;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.shared.response.PageResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 营养建议管理控制器 (管理员专用)
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/nutrition")
public class AdminNutritionController {

    @DubboReference
    private NutritionApi nutritionApi;

    /**
     * 分页查询营养建议
     */
    @GetMapping("/advice")
    public ResponseEntity<ApiResponse<PageResponse<NutritionAdviceResponse>>> listAdvice(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String conditionType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        ListNutritionAdviceQuery query = ListNutritionAdviceQuery.builder()
                .page(page)
                .size(size)
                .conditionType(conditionType)
                .status(status)
                .keyword(keyword)
                .build();

        PageResponse<NutritionAdviceResponse> result = nutritionApi.listAdvice(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 创建营养建议
     */
    @PostMapping("/advice")
    public ResponseEntity<ApiResponse<NutritionAdviceResponse>> createAdvice(
            @Valid @RequestBody CreateNutritionAdviceCommand command) {
        NutritionAdviceResponse response = nutritionApi.createAdvice(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新营养建议
     */
    @PutMapping("/advice/{id}")
    public ResponseEntity<ApiResponse<NutritionAdviceResponse>> updateAdvice(
            @PathVariable Long id,
            @RequestBody UpdateNutritionAdviceCommand command) {
        command.setId(id);
        NutritionAdviceResponse response = nutritionApi.updateAdvice(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除营养建议
     */
    @DeleteMapping("/advice/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdvice(@PathVariable Long id) {
        nutritionApi.deleteAdvice(DeleteNutritionAdviceCommand.builder().id(id).build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
