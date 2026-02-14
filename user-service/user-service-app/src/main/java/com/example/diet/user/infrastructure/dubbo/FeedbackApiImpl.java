package com.example.diet.user.infrastructure.dubbo;

import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.feedback.FeedbackApi;
import com.example.diet.user.api.feedback.command.*;
import com.example.diet.user.api.feedback.query.*;
import com.example.diet.user.api.feedback.response.FeedbackResponse;
import com.example.diet.user.application.FeedbackApplicationService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 反馈服务 Dubbo 实现
 * 只负责接收请求并委托给应用服务
 */
@DubboService
@RequiredArgsConstructor
public class FeedbackApiImpl implements FeedbackApi {

    private final FeedbackApplicationService feedbackApplicationService;

    // ==================== 命令操作 ====================

    @Override
    public FeedbackResponse createFeedback(CreateFeedbackCommand command) {
        return feedbackApplicationService.createFeedback(command);
    }

    @Override
    public FeedbackResponse updateFeedbackStatus(UpdateFeedbackStatusCommand command) {
        return feedbackApplicationService.updateFeedbackStatus(command);
    }

    @Override
    public void deleteFeedback(DeleteFeedbackCommand command) {
        feedbackApplicationService.deleteFeedback(command);
    }

    // ==================== 查询操作 ====================

    @Override
    public FeedbackResponse getFeedback(GetFeedbackQuery query) {
        return feedbackApplicationService.getFeedback(query);
    }

    @Override
    public PageResponse<FeedbackResponse> listUserFeedbacks(ListUserFeedbacksQuery query) {
        return feedbackApplicationService.listUserFeedbacks(query);
    }

    @Override
    public PageResponse<FeedbackResponse> listAllFeedbacks(ListAllFeedbacksQuery query) {
        return feedbackApplicationService.listAllFeedbacks(query);
    }
}
