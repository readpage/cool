package com.example.domain.model;

import lombok.Data;
import java.util.List;

/**
 * 报告定义 — 存储在 sys_config 表 configValue 中（JSON 格式）
 *
 * <pre>
 *   configGroup = 'report'
 *   configKey   = reportId (如 "report_001")
 *   configValue = JSON(ReportDefinition)
 * </pre>
 */
@Data
public class ReportDefinition {
    /** 报告唯一标识，如 "report_001" */
    private String id;
    /** 报告名称 */
    private String name;
    /** 描述 */
    private String description;
    /** SQL 模板（支持 {{key}} / #{key} / [[...]] 语法） */
    private String sqlTemplate;
    /** 参数定义列表 */
    private List<ParamDef> parameters;
}