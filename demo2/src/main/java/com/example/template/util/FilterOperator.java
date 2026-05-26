package com.example.template.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态条件查询操作符 — 生成 :column_N 命名占位符（NamedParameterJdbcTemplate）。
 */
public enum FilterOperator {

    EQ, NE, GT, GE, LT, LE, CONTAINS, BETWEEN, IN;

    /** 生成 :column_N 命名占位符 SQL 片段 + 参数绑定 */
    public String toSql(String column, Object value, String prefix, AtomicInteger idx,
                        Map<String, Object> out) {
        return switch (this) {
            case EQ       -> one(column + " = :",  "", value, prefix, idx, out);
            case NE       -> one(column + " != :", "", value, prefix, idx, out);
            case GT       -> one(column + " > :",  "", value, prefix, idx, out);
            case GE       -> one(column + " >= :", "", value, prefix, idx, out);
            case LT       -> one(column + " < :",  "", value, prefix, idx, out);
            case LE       -> one(column + " <= :", "", value, prefix, idx, out);
            case CONTAINS -> one(column + " LIKE CONCAT('%', :", ", '%')", value, prefix, idx, out);
            case BETWEEN -> {
                List<?> r = (List<?>) value;
                String k1 = prefix + idx.getAndIncrement(); out.put(k1, r.get(0));
                String k2 = prefix + idx.getAndIncrement(); out.put(k2, r.get(1));
                yield column + " BETWEEN :" + k1 + " AND :" + k2;
            }
            case IN -> {
                List<?> l = (List<?>) value;
                java.util.List<String> ks = new java.util.ArrayList<>();
                for (Object v : l) {
                    String k = prefix + idx.getAndIncrement(); out.put(k, v);
                    ks.add(":" + k);
                }
                yield column + " IN (" + String.join(", ", ks) + ")";
            }
        };
    }

    /** 字符串 → 枚举（大小写不敏感） */
    public static FilterOperator fromString(String s) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException("操作符不得为空");
        return switch (s.strip().toLowerCase()) {
            case "eq"       -> EQ;
            case "ne"       -> NE;
            case "gt"       -> GT;
            case "ge"       -> GE;
            case "lt"       -> LT;
            case "le"       -> LE;
            case "contains" -> CONTAINS;
            case "between"  -> BETWEEN;
            case "in"       -> IN;
            default -> throw new IllegalArgumentException("不支持的操作符: " + s);
        };
    }

    private static String one(String before, String after, Object value,
                              String prefix, AtomicInteger idx, Map<String, Object> out) {
        String k = prefix + idx.getAndIncrement();
        out.put(k, value);
        return before + k + after;
    }

    public record FilterCondition(String column, FilterOperator operator, Object value) {}
}
