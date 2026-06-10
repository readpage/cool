package com.example.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * 报告查询结果 — 列元数据 + 分页数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportQueryResult {
    /** 列元数据（prop=模板提取的原名，用于筛选/排序/数据绑定；label=JDBC别名，用于显示） */
    private List<ColumnMeta> columns;
    /** 数据行（key=prop 对应 columns.prop） */
    private List<Map<String, Object>> list;
    /** 总记录数 */
    private long total;
    /** 当前页码 */
    private int current;
    /** 每页大小 */
    private int size;

    /**
     * 列元数据
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColumnMeta {
        /** 列原名（从 SQL 模板提取 AS 前的标识符），用于 WHERE/ORDER BY 和前端数据绑定 */
        private String prop;
        /** 显示名 = JDBC getColumnLabel()（别名如 用户名），可在表格配置中自行修改 */
        private String label;
        /** SQL 类型（java.sql.Types 值），前端可用于自动格式化 */
        private int sqlType;
    }
}
