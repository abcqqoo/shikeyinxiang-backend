-- V9: 创建用户设置与提醒任务表

CREATE TABLE IF NOT EXISTS `user_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `daily_reminder` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '每日提醒开关',
    `nutrition_reminder` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '营养建议提醒开关',
    `water_reminder` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '饮水提醒开关',
    `allow_data_analysis` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '允许数据分析',
    `allow_personalization` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '允许个性化内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_settings_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';

CREATE TABLE IF NOT EXISTS `user_reminder_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `rule_id` BIGINT DEFAULT NULL COMMENT '命中的建议规则ID',
    `title` VARCHAR(100) NOT NULL COMMENT '提醒标题',
    `content` VARCHAR(500) NOT NULL COMMENT '提醒内容',
    `reminder_type` VARCHAR(50) NOT NULL COMMENT '提醒类型',
    `period_key` VARCHAR(32) NOT NULL COMMENT '周期键(如2026-02-11)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0未读 1已读',
    `read_at` DATETIME DEFAULT NULL COMMENT '已读时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_rule_period_type` (`user_id`, `rule_id`, `period_key`, `reminder_type`),
    KEY `idx_user_created_at` (`user_id`, `created_at`),
    KEY `idx_user_status_created_at` (`user_id`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户提醒任务表';
