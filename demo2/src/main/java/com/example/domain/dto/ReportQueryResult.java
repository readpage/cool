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
    /** 列元数据（prop=col_0/col_1...，label=SQL别名） */
    private List<ColumnMeta> columns;
    /** 数据行（key=col_N 对应 columns.prop） */
    private List<Map<String, Object>> list;
    /** 总记录数 */
    private long total;
    /** 当前页码 */
    private int current;
    /** 每页大小 */
    private int size;

    /**
     * 列元数据 — 从 JDBC ResultSetMetaData 自动提取
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColumnMeta {
        /** 内部字段名（col_0, col_1, ...），驱动无关，永远稳定 */
        private String prop;
        /** 显示标签 = SQL getColumnLabel()（别名优先，无别名回退到原始列名） */
        private String label;
        /** SQL 类型（java.sql.Types 值），前端可用于自动格式化 */
        private int sqlType;
    }
}
