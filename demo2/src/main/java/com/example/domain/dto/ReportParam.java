package com.example.domain.dto;

import com.example.template.util.FilterParam;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 报告查询 + 配置参数对象 — 继承 {@link FilterParam}，增加 {@code sqlTemplate}。
 *
 * <p>统一用于即时执行（/execute）和保存 BI 报表配置，前端传 {@code sqlTemplate + filter + sort + current + size + columns}。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportParam extends FilterParam {
    /** SQL 模板（支持 {{filter}} / {{sort}} 占位符） */
    private String sqlTemplate;
}
