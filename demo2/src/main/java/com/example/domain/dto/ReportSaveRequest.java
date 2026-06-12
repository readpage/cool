package com.example.domain.dto;

import com.example.domain.entity.Report;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * 报告读写模型 — 读写复用，前端 ↔ 后端
 *
 * <pre>
 *   包装 Report 实体 + displayConfig（展示配置），同时作为 GET（查询）和 POST（保存）的载体。
 *   后端 Service 层直接拿 report 实体操作，无需逐字段 setter。
 *   displayConfig 使用 JsonNode 纯透传，后端不解析内部字段。
 * </pre>
 */
@Data
public class ReportSaveRequest {

    /** 报告实体（直接映射 report 表） */
    private Report report;

    /** 展示配置（TableConfig 等），后端仅存储/透传 */
    private JsonNode displayConfig;

    /** 关联数据源名称（冗余字段，由 Service 层填充，免去前端全量拉取数据源列表） */
    private String datasourceName;
}
