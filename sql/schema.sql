-- ============================================================
-- LiteBlog 数据库初始化脚本
-- 执行方式（必须带 --default-character-set=utf8mb4，否则中文注释会乱码）：
--   mysql -uroot -p --default-character-set=utf8mb4 < sql/schema.sql
-- 也可以在 IDEA 数据库控制台打开本文件执行（IDEA 默认以 UTF-8 发送）
-- 特点：可重复执行（IF NOT EXISTS），不会清掉已有数据
-- ============================================================

CREATE DATABASE IF NOT EXISTS `liteblog`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `liteblog`;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(32)     NOT NULL COMMENT '登录名',
    `password`   VARCHAR(100)    NOT NULL COMMENT 'BCrypt 密文（固定 60 字符，留余量）',
    `nickname`   VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar`     VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '头像 URL',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`) COMMENT '登录名唯一，同时用于按名查人'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '用户';

-- ------------------------------------------------------------
-- 文章表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '作者',
    `title`      VARCHAR(100)    NOT NULL COMMENT '标题',
    `summary`    VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '摘要（列表页展示，不查正文）',
    `content`    MEDIUMTEXT      NOT NULL COMMENT '正文',
    `status`     VARCHAR(16)     NOT NULL DEFAULT 'published' COMMENT 'draft / published',
    `views`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '阅读数',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_created` (`status`, `created_at`) COMMENT '列表页：已发布文章按时间倒序',
    KEY `idx_user_created` (`user_id`, `created_at`) COMMENT '个人主页：某作者的文章按时间倒序',
    CONSTRAINT `fk_article_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '文章';

-- ------------------------------------------------------------
-- 标签表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tag`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(32)     NOT NULL COMMENT '标签名',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '标签';

-- ------------------------------------------------------------
-- 文章-标签关联表（多对多）
-- 联合主键天然保证同一篇文章不会重复打同一个标签
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article_tag`
(
    `article_id` BIGINT UNSIGNED NOT NULL COMMENT '文章',
    `tag_id`     BIGINT UNSIGNED NOT NULL COMMENT '标签',
    PRIMARY KEY (`article_id`, `tag_id`),
    KEY `idx_tag` (`tag_id`) COMMENT '反向查询：某标签下的文章',
    CONSTRAINT `fk_at_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_at_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '文章-标签关联';

-- ------------------------------------------------------------
-- 评论表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `comment`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_id` BIGINT UNSIGNED NOT NULL COMMENT '所属文章',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '评论人',
    `content`    VARCHAR(500)    NOT NULL COMMENT '内容',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_created` (`article_id`, `created_at`) COMMENT '文章详情页：按时间正序列出评论',
    CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '评论';

-- ------------------------------------------------------------
-- 点赞表
-- 联合主键从数据库层面防止重复点赞（并发下也可靠）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article_like`
(
    `article_id` BIGINT UNSIGNED NOT NULL COMMENT '文章',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '点赞人',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`article_id`, `user_id`),
    KEY `idx_user` (`user_id`) COMMENT '查询某用户点过哪些文章',
    CONSTRAINT `fk_like_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '文章点赞';
