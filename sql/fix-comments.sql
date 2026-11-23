-- ============================================================
-- 修复表/列注释乱码（一次性脚本，只改注释，不动数据）
-- 用途：之前建表时客户端字符集不是 utf8mb4，中文注释写歪了
-- 用法：在 IDEA 数据库控制台选中全部语句执行，或
--       mysql -uroot -p --default-character-set=utf8mb4 < sql/fix-comments.sql
-- ============================================================

ALTER TABLE `user`
    COMMENT = '用户',
    MODIFY COLUMN `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    MODIFY COLUMN `username`   VARCHAR(32)  NOT NULL COMMENT '登录名',
    MODIFY COLUMN `password`   VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文（固定 60 字符，留余量）',
    MODIFY COLUMN `nickname`   VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '昵称',
    MODIFY COLUMN `avatar`     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像 URL',
    MODIFY COLUMN `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE `article`
    COMMENT = '文章',
    MODIFY COLUMN `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    MODIFY COLUMN `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '作者',
    MODIFY COLUMN `title`      VARCHAR(100)    NOT NULL COMMENT '标题',
    MODIFY COLUMN `summary`    VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '摘要（列表页展示，不查正文）',
    MODIFY COLUMN `content`    MEDIUMTEXT      NOT NULL COMMENT '正文',
    MODIFY COLUMN `status`     VARCHAR(16)     NOT NULL DEFAULT 'published' COMMENT 'draft / published',
    MODIFY COLUMN `views`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '阅读数',
    MODIFY COLUMN `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE `tag`
    COMMENT = '标签',
    MODIFY COLUMN `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    MODIFY COLUMN `name`       VARCHAR(32) NOT NULL COMMENT '标签名',
    MODIFY COLUMN `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

ALTER TABLE `article_tag`
    COMMENT = '文章-标签关联',
    MODIFY COLUMN `article_id` BIGINT UNSIGNED NOT NULL COMMENT '文章',
    MODIFY COLUMN `tag_id`     BIGINT UNSIGNED NOT NULL COMMENT '标签';

ALTER TABLE `comment`
    COMMENT = '评论',
    MODIFY COLUMN `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    MODIFY COLUMN `article_id` BIGINT UNSIGNED NOT NULL COMMENT '所属文章',
    MODIFY COLUMN `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '评论人',
    MODIFY COLUMN `content`    VARCHAR(500) NOT NULL COMMENT '内容',
    MODIFY COLUMN `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

ALTER TABLE `article_like`
    COMMENT = '文章点赞',
    MODIFY COLUMN `article_id` BIGINT UNSIGNED NOT NULL COMMENT '文章',
    MODIFY COLUMN `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '点赞人',
    MODIFY COLUMN `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- 执行完自动回显验证：comment 列应该能正常显示中文
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'liteblog';
