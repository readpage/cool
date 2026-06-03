package com.example.domain.dto;

import com.example.domain.entity.Report;
import com.example.domain.model.ReportDisplayConfig;
import lombok.Data;

/**
 * 报告读写模型 — 读写复用，前端 ↔ 后端
 *
 * <pre>
 *   包装 Report 实体 + ReportDisplayConfig，同时作为 GET（查询）和 POST（保存）的载体。
 *   后端 Service 层直接拿 report 实体操作，无需逐字段 setter。
 * </pre>
 */
@Data
public class ReportSaveRequest {

    /** 报告实体（直接映射 report 表） */
    private Report report;

    /** 展示配置（filter + sort + tableConfig），存 sys_config */
    private ReportDisplayConfig displayConfig;
}
