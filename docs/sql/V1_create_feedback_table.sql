-- 用户反馈表
-- 用于存储用户提交的意见反馈

CREATE TABLE `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '提交用户ID',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '反馈类型: 0=建议, 1=问题, 2=其他',
  `content` TEXT NOT NULL COMMENT '反馈内容',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式(可选)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理, 1=处理中, 2=已解决, 3=已关闭',
  `admin_reply` TEXT DEFAULT NULL COMMENT '管理员回复',
  `replied_at` DATETIME DEFAULT NULL COMMENT '回复时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_feedback_user_id` (`user_id`),
  INDEX `idx_feedback_status` (`status`),
  INDEX `idx_feedback_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';
