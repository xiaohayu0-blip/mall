-- Flyway baseline: 现有数据库 schema 快照（2026-06-10）
-- 此文件因 baseline-on-migrate=true 不会被执行，仅作存档。
-- 后续表结构变更请新建 V2__xxx.sql。

CREATE TABLE IF NOT EXISTS `category` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name`        varchar(255) NOT NULL,
  `sort_order`  int          DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `commodity` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `category_id` bigint       DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name`        varchar(255) DEFAULT NULL,
  `price`       bigint       DEFAULT NULL,
  `stock`       int          DEFAULT NULL,
  `status`      int          NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `commodity_tag` (
  `id`           bigint    NOT NULL AUTO_INCREMENT,
  `commodity_id` bigint    NOT NULL,
  `tag_id`       bigint    NOT NULL,
  `tag_group_id` bigint    NOT NULL,
  `create_time`  bigint    NOT NULL,
  `update_time`  bigint    NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_commodity_id` (`commodity_id`),
  KEY `idx_tag_id`       (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tag_group` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `tag_group_name` varchar(255) DEFAULT NULL,
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tag` (
  `id`           bigint       NOT NULL AUTO_INCREMENT,
  `tag_name`     varchar(255) DEFAULT NULL,
  `tag_group_id` bigint       DEFAULT NULL,
  `tag_value`    bigint       DEFAULT NULL,
  `create_time`  bigint       NOT NULL,
  `update_time`  bigint       NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `users` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `username`    varchar(255) DEFAULT NULL,
  `password`    varchar(255) NOT NULL,
  `salt`        varchar(255) DEFAULT NULL,
  `role`        varchar(255) NOT NULL,
  `create_time` bigint       NOT NULL,
  `update_time` bigint       NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `orders` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `order_no`         varchar(255) NOT NULL,
  `user_id`          bigint       NOT NULL,
  `total_amount`     bigint       NOT NULL,
  `status`           varchar(255) NOT NULL,
  `receiver_name`    varchar(255) DEFAULT NULL,
  `receiver_phone`   varchar(255) DEFAULT NULL,
  `receiver_address` varchar(255) DEFAULT NULL,
  `remark`           varchar(255) DEFAULT NULL,
  `paid_time`        bigint       DEFAULT NULL,
  `shipped_time`     bigint       DEFAULT NULL,
  `completed_time`   bigint       DEFAULT NULL,
  `cancelled_time`   bigint       DEFAULT NULL,
  `create_time`      bigint       NOT NULL,
  `update_time`      bigint       NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg8pohnngqi5x1nask7nff2u7w` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order_item` (
  `id`              bigint       NOT NULL AUTO_INCREMENT,
  `order_id`        bigint       NOT NULL,
  `commodity_id`    bigint       NOT NULL,
  `commodity_name`  varchar(255) NOT NULL,
  `commodity_price` bigint       NOT NULL,
  `quantity`        int          NOT NULL,
  `subtotal`        bigint       NOT NULL,
  `create_time`     bigint       NOT NULL,
  `update_time`     bigint       NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `likes_business` (
  `id`            int          NOT NULL AUTO_INCREMENT,
  `business_name` varchar(100) NOT NULL,
  `description`   text         DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `likes_statistic` (
  `id`          bigint NOT NULL AUTO_INCREMENT,
  `business_id` bigint NOT NULL,
  `item_id`     bigint NOT NULL,
  `like_count`  bigint NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_item` (`business_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `likes_user_record` (
  `id`          bigint     NOT NULL AUTO_INCREMENT,
  `user_id`     bigint     NOT NULL,
  `business_id` bigint     NOT NULL,
  `item_id`     bigint     NOT NULL,
  `likes`       tinyint(1) DEFAULT 1 COMMENT '1=点赞, 0=取消',
  `create_time` bigint     NOT NULL,
  `update_time` bigint     NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_business_item` (`user_id`, `business_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
