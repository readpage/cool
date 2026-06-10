package com.example.template.util;

import cn.undraw.handler.exception.customer.CustomerException;
import com.example.template.util.FilterOperator.FilterCondition;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
@JsonIgnoreProperties(ignoreUnknown = true)
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

    /** 是否开启分页（不参与 JSON 序列化，由后端 startPage() 控制） */
    @JsonIgnore
    private transient boolean paginate = false;

    /** 分页结果总数（由 QueryProxyFactory 执行 COUNT 后自动填充） */
    @JsonIgnore
    private transient long total;

    // ==================== 分页控制 ====================

    /**
     * 开启分页。current/size 已由前端 JSON 反序列化填充，
     * 此方法仅翻开关，告诉 QueryProxyFactory 自动执行 COUNT + LIMIT。
     */
    public FilterParam startPage() {
        this.paginate = true;
        return this;
    }

    // ==================== 内部类 ====================

    @Data
    @Accessors(chain = true)
    public static class FilterItem {
        private String column;
        private String operator;
        private Object value;

        /** 标记为 SQL 模板变量（如 #{year}→2025），不生成 WHERE，只做文本插值（自动防注入） */
        @JsonAlias("variable")
        private Boolean variable;
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

        /**
         * 前端组件类型：input / date-range / remote-select 等
         */
        private String fieldType;

        /**
         * 远程选项加载标识（fieldType='remote-select' 时，前端传）
         * 用作 loadOptions(type) 的 type 参数，解决不同表同名字段的选项冲突。
         * 例如用户表 sex → "user_sex"，宠物表 sex → "pet_sex"。
         * 后端导入时用于匹配选项映射。
         */
        private String optionType;
    }

    /**
     * DAO 结果 — 包含 filter/sort 片段 + :column_N 命名参数（含变量值，通过 JDBC 天然防注入）。
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
        // 前端传 camelCase（如 createTime），统一转 snake_case（如 create_time）与 SQL 列白名单匹配
        if (filter != null) {
            for (FilterItem f : filter) {
                f.setColumn(ColumnExtractor.toUnderScoreCase(f.getColumn()));
            }
        }
        if (sort != null && sort.getColumn() != null) {
            sort.setColumn(ColumnExtractor.toUnderScoreCase(sort.getColumn()));
        }

        // SQL 模板提取列（用于校验）：统一取原名（prop），与前端 filter.column 原生列名一致
        List<String> sqlColumns = ColumnExtractor.lastExtract(sqlTemplate);
        validateColumns(sqlColumns);

        // all 搜索时使用的列：前端 camelCase 转 snake_case 后与 SQL 白名单取交集，防注入
        List<String> allSearchColumns;
        if (this.columns != null && !this.columns.isEmpty()) {
            allSearchColumns = this.columns.stream()
                    .map(ColumnItem::getProp)
                    .filter(Objects::nonNull)
                .map(ColumnExtractor::toUnderScoreCase)   // camelCase → snake_case
                .filter(col -> sqlColumns.contains(col)
                        || sqlColumns.stream().anyMatch(c -> ColumnExtractor.stripTablePrefix(c).equalsIgnoreCase(ColumnExtractor.stripTablePrefix(col))))
                .toList();
        } else {
            allSearchColumns = sqlColumns;
        }

        // 变量项（variable=true）：值放入 namedParams 作为 JDBC 参数，不生成 WHERE
        Map<String, Object> namedParams = new LinkedHashMap<>();
        if (filter != null) {
            for (FilterItem f : filter) {
                if (Boolean.TRUE.equals(f.getVariable()) && f.getColumn() != null && f.getValue() != null) {
                    namedParams.put(f.getColumn(), f.getValue());
                }
            }
        }

        // 校验：SQL 模板中的必填 #{key} 变量（不在 [[...]] 内的）是否都提供了值
        List<String> requiredKeys = ColumnExtractor.extractRequiredTemplateKeys(sqlTemplate);
        List<String> missing = new ArrayList<>();
        for (String key : requiredKeys) {
            Object val = namedParams.get(key);
            if (val == null || (val instanceof String s && s.isEmpty())) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            throw new CustomerException(
                "模板变量 " + missing.stream().map(k -> "#{" + k + "}").collect(Collectors.joining("、"))
                + " 未提供值，请在筛选栏填写");
        }

        AtomicInteger idx = new AtomicInteger(0);
        StringBuilder where = new StringBuilder();
        boolean first = true;
        for (FilterCondition c : toConditions()) {
            Object val = c.value();
            if (val == null || (val instanceof String s && s.isEmpty())) continue;
            if (val instanceof Collection<?> col && col.isEmpty()) continue;
            if (first) { first = false; }
            else { where.append(" AND "); }
            if ("all".equalsIgnoreCase(c.column())) {
                // 全字段匹配：对每个可见列生成 OR 条件
                if (allSearchColumns.isEmpty()) {
                    where.append("1=0"); // 无可用列，查不出任何数据
                } else {
                    where.append("(");
                    for (int i = 0; i < allSearchColumns.size(); i++) {
                        if (i > 0) where.append(" OR ");
                        where.append(c.operator().toSql(allSearchColumns.get(i), val,
                                allSearchColumns.get(i) + "_", idx, namedParams));
                    }
                    where.append(")");
                }
            } else {
                where.append(c.operator().toSql(c.column(), val, c.column() + "_", idx, namedParams));
            }
        }

        // 筛选 SQL 片段：模板必须自行携带 WHERE 前缀，此处只生成条件
        String filterSql = hasAnyCondition() ? where.toString() : "";

        // 排序 SQL 片段：有值时携带 ORDER BY 关键字，无值时为空字符串
        // 约定：SQL 模板 SELECT 的第一列必须是主键/唯一列（如 id / t.id），用于分页 tie-breaker
        String sortSql = "";
        if (sort != null && sort.getColumn() != null && !sort.getColumn().isBlank()) {
            String dir = "desc".equalsIgnoreCase(sort.getDirection()) ? "DESC" : "ASC";
            sortSql = "ORDER BY " + sort.getColumn() + " " + dir;
            // 分页时追加首列作为 tie-breaker，避免非唯一列排序导致分页重复/遗漏
            // 比较时去掉表前缀（如 t.id ↔ id），兼容多表场景
            if (paginate && !sqlColumns.isEmpty()
                    && !ColumnExtractor.stripTablePrefix(sort.getColumn())
                        .equalsIgnoreCase(ColumnExtractor.stripTablePrefix(sqlColumns.get(0)))) {
                sortSql += ", " + sqlColumns.get(0) + " ASC";
            }
        }

        return new DaoResult(filterSql, sortSql, namedParams);
    }

    // ==================== 公共工具 ====================

    /**
     * 渲染后 SQL 归一化 — filter 片段不再携带 WHERE 前缀（由模板自行编写），
     * 仅需处理空 filter/sort 时留下的悬空关键字。
     *
     * <p>支持模板写法：
     * <ul>
     *   <li>{@code WHERE {{filter}}} — 无筛选时尾部 WHERE 会被清理</li>
     *   <li>{@code WHERE 1=1 AND {{filter}}} — 无筛选时尾部 AND 会被清理</li>
     *   <li>空 sort 悬空子句：模板 {@code {{sort}}} 中 sort 为空 → 清理尾部 ORDER BY</li>
     * </ul>
     */
    public static String cleanRenderedSql(String renderedSql) {
        if (renderedSql == null) return null;
        // 1. 去掉空 filter 导致 WHERE 后面直接跟 ORDER BY 的情况
        renderedSql = renderedSql.replaceAll("(?i)\\bWHERE\\s+(ORDER\\s+BY\\b)", "$1");
        // 1.5 去掉空 filter 导致的悬空 AND/OR 后面直接跟 ORDER BY 的情况
        //      例如：WHERE age = :age2 AND {{filter}} -> 空 filter → WHERE age = :age2 AND ORDER BY
        renderedSql = renderedSql.replaceAll("(?i)\\s+(AND|OR)\\s+(ORDER\\s+BY\\b)", " $2");
        // 2. 去掉空 filter/sort 留下的悬空 WHERE / AND / OR / ORDER BY（尾部）
        renderedSql = renderedSql.replaceAll("(?i)\\s+(WHERE|AND|OR|ORDER\\s+BY)\\s*$", "").trim();
        return renderedSql;
    }

    // ==================== 内部方法 ====================

    private List<FilterCondition> toConditions() {
        if (filter == null || filter.isEmpty()) return List.of();
        return filter.stream()
                .filter(f -> !Boolean.TRUE.equals(f.getVariable()))  // 变量项走 JDBC 参数化，不生成 WHERE
                .map(f -> new FilterCondition(f.getColumn(),
                        FilterOperator.fromString(f.getOperator()), f.getValue()))
                .toList();
    }

    private void validateColumns(List<String> allowed) {
        if (filter != null)
            filter.forEach(f -> {
                if (!Boolean.TRUE.equals(f.getVariable()))  // 变量项不参与列校验
                    ColumnExtractor.checkColumn(allowed, f.getColumn());
            });
        if (sort != null && sort.getColumn() != null)
            ColumnExtractor.checkColumn(allowed, sort.getColumn());
    }

    private boolean hasAnyCondition() {
        for (FilterCondition c : toConditions()) {
            Object val = c.value();
            if (val != null && (!(val instanceof String s) || !s.isEmpty())
                    && !(val instanceof Collection<?> col && col.isEmpty())) return true;
        }
        return false;
    }
}
