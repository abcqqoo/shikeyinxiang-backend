package com.example.diet.gateway.interfaces.rest;

import com.example.diet.nutrition.api.NutritionApi;
import com.example.diet.nutrition.api.query.GetArticleDetailQuery;
import com.example.diet.nutrition.api.query.ListPublishedArticleQuery;
import com.example.diet.nutrition.api.response.NutritionArticleResponse;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.shared.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 营养文章控制器 (小程序端 - 公开接口)
 */
@Slf4j
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @DubboReference
    private NutritionApi nutritionApi;

    /**
     * 获取已发布文章列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NutritionArticleResponse>>> listPublishedArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        ListPublishedArticleQuery query = ListPublishedArticleQuery.builder()
                .page(page)
                .size(size)
                .build();

        PageResponse<NutritionArticleResponse> result = nutritionApi.listPublishedArticles(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取文章详情 (自动增加浏览量)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionArticleResponse>> getArticleDetail(
            @PathVariable Long id) {

        GetArticleDetailQuery query = GetArticleDetailQuery.builder()
                .id(id)
                .incrementView(true)
                .build();

        NutritionArticleResponse response = nutritionApi.getArticleDetail(query);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
