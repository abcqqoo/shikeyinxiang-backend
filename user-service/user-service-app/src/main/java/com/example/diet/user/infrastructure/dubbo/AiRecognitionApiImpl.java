package com.example.diet.user.infrastructure.dubbo;

import com.example.diet.user.api.ai.AiRecognitionApi;
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
import com.example.diet.user.application.AiRecognitionApplicationService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * AI 识别分析 Dubbo 实现
 */
@DubboService
@RequiredArgsConstructor
public class AiRecognitionApiImpl implements AiRecognitionApi {

    private final AiRecognitionApplicationService aiRecognitionApplicationService;

    @Override
    public Long createTask(CreateAiRecognitionTaskCommand command) {
        return aiRecognitionApplicationService.createTask(command);
    }

    @Override
    public void completeTask(CompleteAiRecognitionTaskCommand command) {
        aiRecognitionApplicationService.completeTask(command);
    }

    @Override
    public void failTask(FailAiRecognitionTaskCommand command) {
        aiRecognitionApplicationService.failTask(command);
    }

    @Override
    public AiRecognitionStatsResponse getStats() {
        return aiRecognitionApplicationService.getStats();
    }

    @Override
    public AiRecognitionTrendResponse getTrend(GetAiRecognitionTrendQuery query) {
        return aiRecognitionApplicationService.getTrend(query);
    }

    @Override
    public List<AiRecognitionStatusDistributionResponse> getStatusDistribution() {
        return aiRecognitionApplicationService.getStatusDistribution();
    }

    @Override
    public List<AiRecognitionTopFoodResponse> getTopFoods(GetAiRecognitionTopFoodsQuery query) {
        return aiRecognitionApplicationService.getTopFoods(query);
    }

    @Override
    public AiRecognitionTaskPageResponse listTasks(ListAiRecognitionTasksQuery query) {
        return aiRecognitionApplicationService.listTasks(query);
    }
}
