package com.example.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报告摘要 — 仅提供给左侧列表使用，不含 sqlTemplate / permissionConfig / displayConfig
 */
@Data
public class ReportSummary {
    private Long id;
    private String tableKey;
    private String name;
    private String description;
    private String category;
    private String displayType;
    private String datasourceName;
    private LocalDateTime updateTime;
}
