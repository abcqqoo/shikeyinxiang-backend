-- V8: 创建用户体重/腰围记录表

CREATE TABLE IF NOT EXISTS `user_body_metrics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_date` DATE NOT NULL COMMENT '记录日期',
    `weight_kg` DECIMAL(6,2) NULL COMMENT '体重(kg)',
    `waist_cm` DECIMAL(6,2) NULL COMMENT '腰围(cm)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `record_date`),
    KEY `idx_user_record_date` (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户体重/腰围记录表';
