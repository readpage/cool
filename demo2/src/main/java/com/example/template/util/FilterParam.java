package com.example.template.util;

import com.example.template.util.FilterOperator.FilterCondition;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态筛选 + 排序请求参数 — 对应前端 JSON，实现 {@link SqlParamProvider} 直接传给 DAO。
 *
 * <pre>{@code
 * String json = """
 *     { "filter": [{ "column": "name", "operator": "contains", "value": "李" }],
 *       "sort": { "column": "id", "direction": "asc" } }""";
 * FilterParam params = mapper.readValue(json, FilterParam.class);
 * List<User> users = userDao.dynamicList(params);
 * }</pre>
 */
@Data
@Accessors(chain = true)
public class FilterParam implements SqlParamProvider {

    /** 筛选条件列表 */
    @JsonAlias("filters")
    private List<FilterItem> filter;

    /** 排序规则（单字段） */
    private SortItem sort;

    /** 当前页码（从 1 开始），默认 1 */
    private int current = 1;

    /** 每页条数，默认 10；设为 0 表示不分页 */
    private int size = 10;

    /** 导出列定义（可选，仅导出时传） */
    private List<ColumnItem> columns;

    // ==================== 内部类 ====================

    @Data
    @Accessors(chain = true)
    public static class FilterItem {
        private String column;
        private String operator;
        private Object value;
    }

    @Data
    @Accessors(chain = true)
    public static class SortItem {
        private String column;
        private String direction;
    }

    @Data
    @Accessors(chain = true)
    public static class ColumnItem {
        /** 实体字段名（如 username / createTime），通过 getter 取值 */
        private String prop;
        /** 表头显示名 */
        private String label;
        /** 列宽（像素） */
        private Integer width;
        /** 最小列宽（备用） */
        private Integer minWidth;
        /** 对齐方式：left/center/right */
        private String align;
    }

    /**
     * DAO 结果 — 包含 filter/sort 片段 + :column_N 命名参数。
     */
    public record DaoResult(String filter, String sort, Map<String, Object> params) {
        public Map<String, Object> toFlatMap() {
            Map<String, Object> m = new LinkedHashMap<>(params);
            m.put("filter", filter);
            m.put("sort", sort);
            return m;
        }
    }

    // ==================== SqlParamProvider ====================

    @Override
    public Map<String, Object> toParamMap(String sqlTemplate) {
        return buildForDao(sqlTemplate).toFlatMap();
    }

    // ==================== 核心生成 ====================

    /**
     * 从 SQL 模板提取列白名单 → 校验 → 生成 filter/sort 片段 + :column_N 命名参数。
     * 模板中可用 {@code {{filter}}} / {@code {{sort}}} 占位符。
     */
    public DaoResult buildForDao(String sqlTemplate) {
        List<String> columns = ColumnExtractor.lastExtract(sqlTemplate);
        validateColumns(columns);

        Map<String, Object> namedParams = new LinkedHashMap<>();
        AtomicInteger idx = new AtomicInteger(0);
        StringBuilder where = new StringBuilder();
        boolean first = true;
        for (FilterCondition c : toConditions()) {
            Object val = c.value();
            if (val == null || (val instanceof String s && s.isEmpty())) continue;
            if (first) { first = false; }
            else { where.append(" AND "); }
            if ("all".equalsIgnoreCase(c.column())) {
                // 全字段匹配：对每个列生成 OR 条件
                if (columns.isEmpty()) {
                    where.append("1=0"); // 无可用列，查不出任何数据
                } else {
                    where.append("(");
                    for (int i = 0; i < columns.size(); i++) {
                        if (i > 0) where.append(" OR ");
                        where.append(c.operator().toSql(columns.get(i), val,
                                columns.get(i) + "_", idx, namedParams));
                    }
                    where.append(")");
                }
            } else {
                where.append(c.operator().toSql(c.column(), val, c.column() + "_", idx, namedParams));
            }
        }

        StringBuilder orderBy = new StringBuilder();
        if (sort != null) {
            String dir = "desc".equalsIgnoreCase(sort.getDirection()) ? "DESC" : "ASC";
            orderBy.append("ORDER BY ").append(sort.getColumn()).append(" ").append(dir);
        }

        return new DaoResult(
                hasAnyCondition() ? where.toString() : "",
                orderBy.toString(),
                namedParams
        );
    }

    // ==================== 内部方法 ====================

    private List<FilterCondition> toConditions() {
        if (filter == null || filter.isEmpty()) return List.of();
        return filter.stream()
                .map(f -> new FilterCondition(f.getColumn(),
                        FilterOperator.fromString(f.getOperator()), f.getValue()))
                .toList();
    }

    private void validateColumns(List<String> allowed) {
        if (filter != null)
            filter.forEach(f -> ColumnExtractor.checkColumn(allowed, f.getColumn()));
        if (sort != null && sort.getColumn() != null)
            ColumnExtractor.checkColumn(allowed, sort.getColumn());
    }

    private boolean hasAnyCondition() {
        for (FilterCondition c : toConditions()) {
            Object val = c.value();
            if (val != null && (!(val instanceof String s) || !s.isEmpty())) return true;
        }
        return false;
    }
}
