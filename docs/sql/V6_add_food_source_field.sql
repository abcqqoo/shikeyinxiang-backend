-- 为 AI 估算食物支持扩展饮食记录表结构
-- 目标表：diet_record_foods

-- 1) 允许 food_id 为空（AI估算食物可能没有对应数据库食物）
ALTER TABLE `diet_record_foods`
    MODIFY COLUMN `food_id` BIGINT NULL;

-- 2) 新增来源字段
ALTER TABLE `diet_record_foods`
    ADD COLUMN `source` VARCHAR(20) NOT NULL DEFAULT 'database'
    COMMENT '食物来源: database-数据库食物, ai_estimated-AI估算食物'
    AFTER `carbs`;

-- 3) 历史数据回填（默认归类为数据库食物）
UPDATE `diet_record_foods`
SET `source` = 'database'
WHERE `source` IS NULL OR `source` = '';
