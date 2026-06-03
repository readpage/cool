-- ============================================================
-- 系统默认配置表 — 仅存系统级配置（已拆分用户偏好到 user_config 表）
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    config_group    VARCHAR(64)   NOT NULL COMMENT '配置分组：table / bi / system / report',
    config_key      VARCHAR(128)  NOT NULL COMMENT '配置标识：user / report_001 / theme',
    config_value    JSON          NOT NULL COMMENT '配置 JSON 内容',
    version         INT           NOT NULL DEFAULT 0 COMMENT '版本号（单调递增）',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    UNIQUE KEY uk_config (config_group, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统默认配置表';

-- ============================================================
-- 用户偏好配置表 — 用户级自定义配置
-- ============================================================
CREATE TABLE IF NOT EXISTS user_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    config_group    VARCHAR(64)   NOT NULL COMMENT '配置分组',
    config_key      VARCHAR(128)  NOT NULL COMMENT '配置标识',
    user_id         BIGINT        NOT NULL COMMENT '用户ID',
    config_value    JSON          NOT NULL COMMENT '配置 JSON 内容',
    deleted         TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '0=正常, 1=回收站（软删除）',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    UNIQUE KEY uk_user_config (config_group, config_key, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好配置表';

-- ============================================================
-- 系统默认配置变更历史表 — 支持版本回退与审计
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_config_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    config_group    VARCHAR(64)   NOT NULL COMMENT '配置分组',
    config_key      VARCHAR(128)  NOT NULL COMMENT '配置标识',
    version         INT           NOT NULL COMMENT '快照版本号（单调递增）',
    snapshot        JSON          NOT NULL COMMENT '该版本 config_value 全量快照',
    change_type     VARCHAR(16)   NOT NULL COMMENT 'SAVE(保存) / ROLLBACK(回退)',
    rollback_from   INT           NULL     COMMENT '回退来源版本号（change_type=ROLLBACK 时记录）',
    changed_by      BIGINT        NOT NULL COMMENT '操作人 ID',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    UNIQUE KEY uk_history (config_group, config_key, version),
    INDEX idx_timeline (config_group, config_key, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统默认配置变更历史表';
