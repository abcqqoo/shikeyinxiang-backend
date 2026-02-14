-- AI食物识别任务表
-- 用于存储用户的AI图像识别请求和结果

CREATE TABLE `ai_recognition_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '发起识别的用户ID',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '原始图片URL（如已存储）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processing/completed/failed',
  `model_name` VARCHAR(50) DEFAULT NULL COMMENT '使用的AI模型（如 gemini-2.0-flash）',
  `total_items` INT DEFAULT 0 COMMENT '识别出的食物数量',
  `processing_time_ms` INT DEFAULT NULL COMMENT '处理耗时（毫秒）',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '失败时的错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_ai_recognition_task_user_id` (`user_id`),
  INDEX `idx_ai_recognition_task_status` (`status`),
  INDEX `idx_ai_recognition_task_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI食物识别任务表';

-- AI食物识别结果项表
-- 用于存储每次识别任务中识别出的具体食物项

CREATE TABLE `ai_recognition_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL COMMENT '关联的识别任务ID',
  `food_name` VARCHAR(100) NOT NULL COMMENT '识别出的食物名称',
  `confidence` DECIMAL(5,4) NOT NULL COMMENT '置信度 0.0000-1.0000',
  `calories` DECIMAL(10,2) DEFAULT NULL COMMENT '估算热量（千卡）',
  `protein_g` DECIMAL(10,2) DEFAULT NULL COMMENT '蛋白质（克）',
  `fat_g` DECIMAL(10,2) DEFAULT NULL COMMENT '脂肪（克）',
  `carbs_g` DECIMAL(10,2) DEFAULT NULL COMMENT '碳水化合物（克）',
  `estimated_grams` INT DEFAULT NULL COMMENT '估算份量（克）',
  `was_selected` TINYINT(1) DEFAULT 0 COMMENT '用户是否选择了此项',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_ai_recognition_item_task_id` (`task_id`),
  INDEX `idx_ai_recognition_item_confidence` (`confidence`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI食物识别结果项表';
