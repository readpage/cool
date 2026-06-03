package com.example.domain.model;

import com.example.template.util.FilterParam;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 报表展示配置 — 存储在 sys_config / user_config 的 configValue 中
 *
 * <pre>
 *   与报告定义分离：
 *   - Report (report 表)：元信息 + sqlTemplate + 权限（列表查询用）
 *   - ReportDisplayConfig (sys_config/user_config)：filter / sort / tableConfig（展示偏好，支持用户覆盖）
 *
 *   configGroup = 'report_display'
 *   configKey   = report.table_key
 *   configValue = JSON(ReportDisplayConfig)
 * </pre>
 */
@Data
public class ReportDisplayConfig {

    /** 默认筛选条件列表 */
    private List<FilterParam.FilterItem> filter;

    /** 默认排序 */
    private FilterParam.SortItem sort;

    /** 表格配置 */
    private TableConfig tableConfig;

    // ===== inner types =====

    @Data
    public static class TableConfig {

        /** 列配置列表 */
        private List<ColumnConfig> columns;

        /** 是否显示斑马纹 */
        private Boolean stripe;

        /** 表格尺寸：large | default | small */
        private String size;

        /** 固定高度 */
        private String height;

        /** 最大高度 */
        private String maxHeight;

        /** 行 key 字段 */
        private String rowKey;

        // ===== inner =====

        @Data
        public static class ColumnConfig {
            private String prop;
            private String label;
            private String width;
            private String minWidth;
            /** 对齐：left | center | right */
            private String align;
            /** 是否隐藏 */
            private Boolean hidden;
            /** 是否可排序 */
            private Boolean sortable;
            /** 列序 */
            private Integer order;
        }
    }
}
