package com.example.diet.user.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.exception.ErrorCode;
import com.example.diet.shared.response.PageResponse;
import com.example.diet.user.api.feedback.command.*;
import com.example.diet.user.api.feedback.query.*;
import com.example.diet.user.api.feedback.response.FeedbackResponse;
import com.example.diet.user.infrastructure.persistence.mapper.FeedbackMapper;
import com.example.diet.user.infrastructure.persistence.po.FeedbackPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 反馈应用服务
 * 处理用户反馈相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackApplicationService {

    private final FeedbackMapper feedbackMapper;

    // ==================== 命令操作 ====================

    /**
     * 创建反馈
     */
    @Transactional
    public FeedbackResponse createFeedback(CreateFeedbackCommand command) {
        log.info("创建反馈: userId={}, type={}", command.getUserId(), command.getType());

        FeedbackPO po = new FeedbackPO();
        po.setUserId(command.getUserId());
        po.setType(command.getType());
        po.setContent(command.getContent());
        po.setContact(command.getContact());
        po.setStatus(0); // 待处理
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());

        feedbackMapper.insert(po);

        return toResponse(po);
    }

    /**
     * 更新反馈状态/回复 (管理员)
     */
    @Transactional
    public FeedbackResponse updateFeedbackStatus(UpdateFeedbackStatusCommand command) {
        log.info("更新反馈状态: id={}, status={}", command.getId(), command.getStatus());

        FeedbackPO po = feedbackMapper.selectById(command.getId());
        if (po == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "反馈不存在");
        }

        if (command.getStatus() != null) {
            po.setStatus(command.getStatus());
        }

        if (StringUtils.hasText(command.getAdminReply())) {
            po.setAdminReply(command.getAdminReply());
            po.setRepliedAt(LocalDateTime.now());
        }

        po.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.updateById(po);

        return toResponse(po);
    }

    /**
     * 删除反馈
     */
    @Transactional
    public void deleteFeedback(DeleteFeedbackCommand command) {
        log.info("删除反馈: id={}", command.getId());

        FeedbackPO po = feedbackMapper.selectById(command.getId());
        if (po == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "反馈不存在");
        }

        feedbackMapper.deleteById(command.getId());
    }

    // ==================== 查询操作 ====================

    /**
     * 获取单个反馈详情
     */
    public FeedbackResponse getFeedback(GetFeedbackQuery query) {
        FeedbackPO po = feedbackMapper.selectById(query.getId());
        if (po == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "反馈不存在");
        }

        // 如果指定了 userId，验证权限
        if (query.getUserId() != null && !po.getUserId().equals(query.getUserId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "无权查看此反馈");
        }

        return toResponse(po);
    }

    /**
     * 分页查询用户自己的反馈列表
     */
    public PageResponse<FeedbackResponse> listUserFeedbacks(ListUserFeedbacksQuery query) {
        LambdaQueryWrapper<FeedbackPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeedbackPO::getUserId, query.getUserId());
        wrapper.orderByDesc(FeedbackPO::getCreatedAt);

        Page<FeedbackPO> page = new Page<>(query.getPage(), query.getSize());
        IPage<FeedbackPO> result = feedbackMapper.selectPage(page, wrapper);

        List<FeedbackResponse> records = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 分页查询所有反馈列表 (管理员)
     */
    public PageResponse<FeedbackResponse> listAllFeedbacks(ListAllFeedbacksQuery query) {
        LambdaQueryWrapper<FeedbackPO> wrapper = new LambdaQueryWrapper<>();

        // 筛选条件
        if (query.getStatus() != null) {
            wrapper.eq(FeedbackPO::getStatus, query.getStatus());
        }
        if (query.getUserId() != null) {
            wrapper.eq(FeedbackPO::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(FeedbackPO::getContent, query.getKeyword());
        }

        wrapper.orderByDesc(FeedbackPO::getCreatedAt);

        Page<FeedbackPO> page = new Page<>(query.getPage(), query.getSize());
        IPage<FeedbackPO> result = feedbackMapper.selectPage(page, wrapper);

        List<FeedbackResponse> records = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    // ==================== 私有方法 ====================

    /**
     * PO 转 Response
     */
    private FeedbackResponse toResponse(FeedbackPO po) {
        return FeedbackResponse.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .type(po.getType())
                .content(po.getContent())
                .contact(po.getContact())
                .status(po.getStatus())
                .adminReply(po.getAdminReply())
                .repliedAt(po.getRepliedAt())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
}
