package com.example.diet.user.api.ai;

import com.example.diet.user.api.ai.command.CompleteAiRecognitionTaskCommand;
import com.example.diet.user.api.ai.command.CreateAiRecognitionTaskCommand;
import com.example.diet.user.api.ai.command.FailAiRecognitionTaskCommand;
import com.example.diet.user.api.ai.query.GetAiRecognitionTopFoodsQuery;
import com.example.diet.user.api.ai.query.GetAiRecognitionTrendQuery;
import com.example.diet.user.api.ai.query.ListAiRecognitionTasksQuery;
import com.example.diet.user.api.ai.response.AiRecognitionStatsResponse;
import com.example.diet.user.api.ai.response.AiRecognitionStatusDistributionResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTaskPageResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTopFoodResponse;
import com.example.diet.user.api.ai.response.AiRecognitionTrendResponse;

import java.util.List;

/**
 * AI 识别分析 Dubbo API
 * 提供管理端 AI 识别分析页所需数据
 */
public interface AiRecognitionApi {

    /**
     * 创建识别任务
     */
    Long createTask(CreateAiRecognitionTaskCommand command);

    /**
     * 标记识别任务成功并写入识别项
     */
    void completeTask(CompleteAiRecognitionTaskCommand command);

    /**
     * 标记识别任务失败
     */
    void failTask(FailAiRecognitionTaskCommand command);

    /**
     * 获取识别统计概览
     */
    AiRecognitionStatsResponse getStats();

    /**
     * 获取识别趋势
     */
    AiRecognitionTrendResponse getTrend(GetAiRecognitionTrendQuery query);

    /**
     * 获取识别状态分布
     */
    List<AiRecognitionStatusDistributionResponse> getStatusDistribution();

    /**
     * 获取热门识别食物
     */
    List<AiRecognitionTopFoodResponse> getTopFoods(GetAiRecognitionTopFoodsQuery query);

    /**
     * 分页查询识别任务
     */
    AiRecognitionTaskPageResponse listTasks(ListAiRecognitionTasksQuery query);
}
