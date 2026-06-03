-- ========================================
-- BI 报表（问题）表
-- ========================================
CREATE TABLE IF NOT EXISTS `report` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `table_key`        VARCHAR(64)   NOT NULL COMMENT '报表业务唯一标识（对应 sys_config / user_config 的 config_key）',
    `name`             VARCHAR(200)  NOT NULL COMMENT '报告名称',
    `description`      VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    `category`         VARCHAR(50)   DEFAULT NULL COMMENT '分类',
    `sql_template`     TEXT          NOT NULL COMMENT 'SQL 模板',
    `display_type`     VARCHAR(20)   DEFAULT 'table' COMMENT '展示类型：table|bar|line|pie|number',
    `permission_config` JSON         DEFAULT NULL COMMENT '权限配置 JSON',
    `creator_id`       BIGINT        DEFAULT NULL COMMENT '创建者 ID',
    `deleted`          TINYINT       DEFAULT 0 COMMENT '软删除',
    `create_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_table_key` (`table_key`),
    INDEX `idx_name` (`name`),
    INDEX `idx_category` (`category`),
    INDEX `idx_creator` (`creator_id`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BI 报表（问题）';
