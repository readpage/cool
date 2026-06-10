package com.example.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * BI 报表（问题）— 对应 report 表
 * <pre>
 *   存储报表元信息 + SQL 模板 + 权限
 *   展示配置（filter/sort/tableConfig）通过 sys_config / user_config 管理
 * </pre>
 */
@Data
@TableName("report")
public class Report {

    /** 主键（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报表业务唯一标识，对应 sys_config / user_config 的 config_key */
    private String tableKey;

    /** 报告名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 分类 */
    private String category;

    /** SQL 模板（支持 {{filter}} / {{sort}} / #{key} 语法） */
    private String sqlTemplate;

    /** 展示类型：table | bar | line | pie | number */
    private String displayType;

    /** 权限配置 JSON */
    private String permissionConfig;

    /** 关联数据源 ID，NULL 表示使用主数据源 */
    private Long datasourceId;

    /** 创建者 ID */
    private Long creatorId;

    /** 软删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
