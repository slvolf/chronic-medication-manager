-- ============================================================
-- 慢性病用药健康管理系统 - 数据库表结构设计（MVP第一阶段）
-- 数据库: MySQL 8.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS medication_manage
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE medication_manage;

-- -----------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号（登录凭证）',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱（备用登录凭证）',
  `password`    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `name`        VARCHAR(50)  DEFAULT NULL COMMENT '用户姓名',
  `age`         INT          DEFAULT NULL COMMENT '年龄',
  `gender`      TINYINT      DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 用药计划表
-- -----------------------------------------------------------
CREATE TABLE `medication_plan` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`           BIGINT       NOT NULL COMMENT '用户ID（关联user表）',
  `drug_name`         VARCHAR(100) NOT NULL COMMENT '药品名称',
  `dosage`            VARCHAR(50)  DEFAULT NULL COMMENT '服用剂量（如"1片"、"5ml"）',
  `frequency_mode`    TINYINT      NOT NULL COMMENT '频次模式：1-每日固定次数 2-每日固定时间',
  `frequency_times`   INT          DEFAULT NULL COMMENT '每日次数（mode=1时使用）',
  `start_date`        DATE         NOT NULL COMMENT '开始日期',
  `end_date`          DATE         DEFAULT NULL COMMENT '结束日期（null表示长期）',
  `remark`            VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted`           TINYINT      DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_plan_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药计划表';

-- -----------------------------------------------------------
-- 3. 提醒时间表（用药计划的每日具体提醒时间点）
-- -----------------------------------------------------------
CREATE TABLE `reminder_time` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id`     BIGINT   NOT NULL COMMENT '用药计划ID（关联medication_plan表）',
  `remind_time` TIME     NOT NULL COMMENT '提醒时间（如08:00、12:30、18:00）',
  `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  CONSTRAINT `fk_reminder_plan` FOREIGN KEY (`plan_id`) REFERENCES `medication_plan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒时间表';

-- -----------------------------------------------------------
-- 4. 用药记录表（打卡记录）
-- -----------------------------------------------------------
CREATE TABLE `medication_record` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
  `plan_id`         BIGINT       NOT NULL COMMENT '用药计划ID',
  `reminder_time_id` BIGINT     DEFAULT NULL COMMENT '提醒时间ID（关联reminder_time表）',
  `record_date`     DATE         NOT NULL COMMENT '记录日期',
  `scheduled_time`  TIME         NOT NULL COMMENT '计划服用时间',
  `actual_time`     DATETIME     DEFAULT NULL COMMENT '实际打卡时间（null表示未打卡）',
  `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-待服药 1-已服药 2-漏服',
  `remark`          VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `deleted`         TINYINT      DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_record_date` (`record_date`),
  KEY `idx_user_date` (`user_id`, `record_date`),
  UNIQUE KEY `uk_plan_date_time` (`plan_id`, `record_date`, `scheduled_time`),
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_record_plan` FOREIGN KEY (`plan_id`) REFERENCES `medication_plan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药记录表（打卡记录）';
