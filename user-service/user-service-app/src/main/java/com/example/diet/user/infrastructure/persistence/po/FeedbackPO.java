package com.example.diet.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户反馈持久化对象
 * 对应数据库 feedback 表
 */
@Data
@TableName("feedback")
public class FeedbackPO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提交用户ID
     */
    private Long userId;

    /**
     * 反馈类型: 0=建议, 1=问题, 2=其他
     */
    private Integer type;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 状态: 0=待处理, 1=处理中, 2=已解决, 3=已关闭
     */
    private Integer status;

    /**
     * 管理员回复
     */
    private String adminReply;

    /**
     * 回复时间
     */
    private LocalDateTime repliedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
