package com.example.diet.gateway.interfaces.rest;

import com.example.diet.nutrition.api.NutritionApi;
import com.example.diet.nutrition.api.command.CreateNutritionArticleCommand;
import com.example.diet.nutrition.api.command.DeleteNutritionArticleCommand;
import com.example.diet.nutrition.api.command.UpdateNutritionArticleCommand;
import com.example.diet.nutrition.api.query.ListNutritionArticleQuery;
import com.example.diet.nutrition.api.response.NutritionArticleResponse;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.shared.response.PageResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 营养文章管理控制器 (管理员专用)
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    @DubboReference
    private NutritionApi nutritionApi;

    /**
     * 分页查询营养文章
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NutritionArticleResponse>>> listArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        ListNutritionArticleQuery query = ListNutritionArticleQuery.builder()
                .page(page)
                .size(size)
                .status(status)
                .keyword(keyword)
                .build();

        PageResponse<NutritionArticleResponse> result = nutritionApi.listArticles(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 创建营养文章
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NutritionArticleResponse>> createArticle(
            @Valid @RequestBody CreateNutritionArticleCommand command) {
        NutritionArticleResponse response = nutritionApi.createArticle(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新营养文章
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionArticleResponse>> updateArticle(
            @PathVariable Long id,
            @RequestBody UpdateNutritionArticleCommand command) {
        command.setId(id);
        NutritionArticleResponse response = nutritionApi.updateArticle(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除营养文章
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        nutritionApi.deleteArticle(DeleteNutritionArticleCommand.builder().id(id).build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
