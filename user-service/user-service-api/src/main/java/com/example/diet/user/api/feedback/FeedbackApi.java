package com.example.diet.user.api.feedback;

import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.feedback.command.*;
import com.example.diet.user.api.feedback.query.*;
import com.example.diet.user.api.feedback.response.FeedbackResponse;

/**
 * 用户反馈服务 Dubbo API
 * 提供反馈提交、查询、管理等功能
 */
public interface FeedbackApi {

    // ==================== 命令操作 (写) ====================

    /**
     * 创建反馈
     */
    FeedbackResponse createFeedback(CreateFeedbackCommand command);

    /**
     * 更新反馈状态/回复
     */
    FeedbackResponse updateFeedbackStatus(UpdateFeedbackStatusCommand command);

    /**
     * 删除反馈
     */
    void deleteFeedback(DeleteFeedbackCommand command);

    // ==================== 查询操作 (读) ====================

    /**
     * 获取单个反馈详情
     */
    FeedbackResponse getFeedback(GetFeedbackQuery query);

    /**
     * 分页查询用户自己的反馈列表
     */
    PageResponse<FeedbackResponse> listUserFeedbacks(ListUserFeedbacksQuery query);

    /**
     * 分页查询所有反馈列表 (管理员)
     */
    PageResponse<FeedbackResponse> listAllFeedbacks(ListAllFeedbacksQuery query);
}
