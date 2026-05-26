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
            where.append(c.operator().toSql(c.column(), val, c.column() + "_", idx, namedParams));
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
