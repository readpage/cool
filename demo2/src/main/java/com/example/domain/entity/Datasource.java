package com.example.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源配置 — 对应 datasource 表
 */
@Data
@TableName("datasource")
public class Datasource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 数据源名称 */
    private String name;

    /** 数据库类型：MYSQL / POSTGRESQL */
    private String dbType;

    /** 主机地址 */
    private String host;

    /** 端口 */
    private Integer port;

    /** 数据库名 */
    private String dbName;

    /** 用户名 */
    private String username;

    /** 密码（后续应做 AES 加密） */
    private String password;

    /** 额外 JDBC 连接参数 */
    private String params;

    /** 状态：1=启用，0=停用 */
    private Integer status;

    /** 软删除 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
