package com.example.template.util;

import java.util.*;
import java.util.regex.*;

/**
 * 从 SQL 模板中提取 SELECT 列名，用于动态列白名单校验防注入。
 */
public class ColumnExtractor {

    private static final Pattern SELECT_FROM = Pattern.compile("(?i)SELECT\\s+(.+?)\\s+FROM", Pattern.DOTALL);
    private static final Pattern AS_ALIAS = Pattern.compile("(?i)\\s+AS\\s+(\\S+)$");
    private static final Pattern SPACE_ALIAS = Pattern.compile("(?i)\\s+([a-zA-Z_]\\w*)$");
    private static final Pattern TABLE_PREFIX = Pattern.compile("^[a-zA-Z_]\\w*\\.");
    private static final Pattern FUNCTION_CALL = Pattern.compile("^\\w+\\s*\\(.*\\)$");

    public static List<String> extract(String template) {
        return doExtract(template);
    }

    /**
     * 从 SQL 模板提取最后一个 SELECT 的列名（兼容 CTE / WITH AS）。
     *
     * <pre>{@code
     * WITH cte AS (SELECT a, b FROM x)
     * SELECT id, name FROM user WHERE {{filter}}
     * }} → [id, name]
     * </pre>
     */
    public static List<String> lastExtract(String template) {
        return doExtract(template);
    }

    private static List<String> doExtract(String template) {
        String sql = template
                .replaceAll("\\{\\{[^}]*}}", "")
                .replaceAll("#\\{[^}]*}", "")
                .replaceAll("\\[\\[.*?]]", " ");
        Matcher m = SELECT_FROM.matcher(sql);
        String clause = null;
        while (m.find()) clause = m.group(1);   // 始终取最后一个 SELECT...FROM
        if (clause == null) return List.of();

        List<String> result = new ArrayList<>();
        for (String part : clause.split(",")) {
            String col = cleanColumnName(part.strip());
            if (!col.isEmpty() && !result.contains(col)) result.add(col);
        }
        return Collections.unmodifiableList(result);
    }

    private static String cleanColumnName(String raw) {
        String col = TABLE_PREFIX.matcher(raw).replaceFirst("").strip();
        if (col.equals("*") || col.isEmpty() || FUNCTION_CALL.matcher(col).matches()) return "";
        Matcher as = AS_ALIAS.matcher(col);
        if (as.find()) return as.group(1);
        Matcher sp = SPACE_ALIAS.matcher(col);
        if (sp.find()) return sp.group(1);
        return col;
    }

    public static void checkColumn(List<String> allowed, String column) {
        if (allowed.isEmpty()) return;
        if (!allowed.contains(column.strip()))
            throw new IllegalArgumentException("非法列名: \"" + column + "\"，允许: " + allowed);
    }
}
