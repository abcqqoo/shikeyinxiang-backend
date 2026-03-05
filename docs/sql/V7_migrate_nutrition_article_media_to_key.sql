-- V7: 将 nutrition_article 的封面与正文媒体由 URL 迁移为文件 key
-- 目标：数据库仅保存 key（例如 article/5/xxx.jpg），不再保存临时预签名 URL

-- 1) 备份（如已存在则跳过）
CREATE TABLE IF NOT EXISTS nutrition_article_backup_20260208 AS
SELECT * FROM nutrition_article;

-- 2) cover: https://host/key?... -> key
UPDATE nutrition_article
SET cover = REGEXP_REPLACE(
    SUBSTRING_INDEX(cover, '?', 1),
    '^https?://[^/]+/',
    ''
)
WHERE cover REGEXP '^https?://';

-- 3) content: 双引号 src/poster URL -> key
UPDATE nutrition_article
SET content = REGEXP_REPLACE(
    content,
    '(src|poster)=\"https?://[^/]+/([^\"?]+)(\\?[^\\\"]*)?\"',
    '$1=\"$2\"'
)
WHERE content REGEXP '(src|poster)=\"https?://';

-- 4) content: 单引号 src/poster URL -> key
UPDATE nutrition_article
SET content = REGEXP_REPLACE(
    content,
    "(src|poster)='https?://[^/]+/([^'?]+)(\\?[^']*)?'",
    "$1='$2'"
)
WHERE content REGEXP "(src|poster)='https?://";

-- 5) 验证
SELECT COUNT(*) AS total,
       SUM(CASE WHEN cover REGEXP '^https?://' THEN 1 ELSE 0 END) AS cover_http,
       SUM(CASE WHEN content REGEXP 'src=\"https?://' OR content REGEXP "src='https?://"
                OR content REGEXP 'poster=\"https?://' OR content REGEXP "poster='https?://"
                THEN 1 ELSE 0 END) AS content_http
FROM nutrition_article;
