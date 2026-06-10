package com.example.template.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从 SQL 模板中提取 SELECT 列名，用于动态列白名单校验防注入。
 */
public class ColumnExtractor {

    private static final Pattern SELECT_FROM = Pattern.compile("(?i)SELECT\\s+(.+?)\\s+FROM", Pattern.DOTALL);
    private static final Pattern AS_ALIAS = Pattern.compile("(?i)\\s+AS\\s+(\\S+)$");
    private static final Pattern SPACE_ALIAS = Pattern.compile("(?i)\\s+([a-zA-Z_]\\w*)$");
    private static final Pattern SQUOTE_ALIAS = Pattern.compile("\\s+'([^']+)'\\s*$");
    private static final Pattern DQUOTE_ALIAS = Pattern.compile("\\s+\"([^\"]+)\"\\s*$");
    private static final Pattern BACKTICK_ALIAS = Pattern.compile("\\s+`([^`]+)`\\s*$");
    private static final Pattern TABLE_PREFIX = Pattern.compile("^[a-zA-Z_]\\w*\\.");
    private static final Pattern FUNCTION_CALL = Pattern.compile("^\\w+\\s*\\(.*\\)$");

    public static List<String> extract(String template) {
        return doExtract(template, true);
    }

    /**
     * 从 SQL 模板提取最后一个 SELECT 的列原名（AS 前面的标识符）。
     * 适用于 WITH + {{filter}} 占位符路径，原名与 CTE 列名一致。
     */
    public static List<String> lastExtract(String template) {
        return doExtract(template, true);
    }

    /**
     * 从 SQL 模板提取最后一个 SELECT 的列别名（AS/引号后面的显示名）。
     * 适用于子查询包装路径，派生表只暴露别名。
     */
    public static List<String> extractAliases(String template) {
        return doExtract(template, false);
    }

    private static List<String> doExtract(String template, boolean extractProp) {
        String sql = template
                .replaceAll("\\{\\{[^}]*}}", "")
                .replaceAll("#\\{[^}]*}", "")
                .replaceAll("\\[\\[.*?]]", " ");
        Matcher m = SELECT_FROM.matcher(sql);
        String clause = null;
        while (m.find()) clause = m.group(1);
        if (clause == null) return List.of();

        List<String> result = new ArrayList<>();
        for (String part : clause.split(",")) {
            String col = cleanColumnName(part.strip(), extractProp);
            if (!col.isEmpty() && !result.contains(col)) result.add(col);
        }
        return Collections.unmodifiableList(result);
    }

    private static String cleanColumnName(String raw, boolean extractProp) {
        String col = raw.strip();
        if (col.equals("*") || col.isEmpty()) return "";

        // 1) AS 别名
        Matcher as = AS_ALIAS.matcher(col);
        if (as.find()) {
            if (extractProp) {
                // 取 AS 前面的原名
                String prop = col.substring(0, as.start()).strip();
                if (!prop.isEmpty() && !FUNCTION_CALL.matcher(prop).matches()) return prop;
                return "";
            } else {
                // 取 AS 后面的别名
                return stripQuotes(as.group(1));
            }
        }

        // 2-4) 引号/反引号别名
        for (Pattern p : new Pattern[]{SQUOTE_ALIAS, DQUOTE_ALIAS, BACKTICK_ALIAS}) {
            Matcher m = p.matcher(col);
            if (m.find()) {
                if (extractProp) {
                    String prop = col.substring(0, m.start()).strip();
                    if (!prop.isEmpty() && !FUNCTION_CALL.matcher(prop).matches()) return prop;
                    return "";
                } else {
                    return m.group(1);
                }
            }
        }

        // 5) 空格别名（纯标识符）: 别名即为可用列名
        Matcher sp = SPACE_ALIAS.matcher(col);
        if (sp.find()) return sp.group(1);

        // 6) 无别名表达式 → 跳过
        if (FUNCTION_CALL.matcher(col).matches()) return "";

        return col;
    }

    /**
     * 去掉别名两端的引号
     */
    private static String stripQuotes(String alias) {
        if (alias == null || alias.length() < 2) return alias;
        char first = alias.charAt(0);
        char last = alias.charAt(alias.length() - 1);
        if ((first == '\'' && last == '\'') || (first == '"' && last == '"') || (first == '`' && last == '`')) {
            return alias.substring(1, alias.length() - 1);
        }
        return alias;
    }


    /**
     * 从 SQL 模板中提取必填 #{key} 变量名（不在 [[...]] 可选块内的）。
     * 用于后端校验：在 SQL 执行前，确保所有必填变量都已提供值。
     */
    public static List<String> extractRequiredTemplateKeys(String sql) {
        if (sql == null || sql.isEmpty()) return List.of();
        List<String> required = new ArrayList<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        int depth = 0;
        int i = 0;
        while (i < sql.length()) {
            if (sql.startsWith("[[", i)) {
                depth++;
                i += 2;
            } else if (sql.startsWith("]]", i) && depth > 0) {
                depth--;
                i += 2;
            } else if (sql.startsWith("#{", i)) {
                int end = sql.indexOf('}', i + 2);
                if (end > i) {
                    String key = sql.substring(i + 2, end).trim();
                    if (depth == 0 && seen.add(key)) {
                        required.add(key);
                    }
                    i = end + 1;
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }
        return Collections.unmodifiableList(required);
    }

    public static void checkColumn(List<String> allowed, String column) {
        if (allowed.isEmpty()) return;
        if ("all".equalsIgnoreCase(column.strip())) return;   // 全字段匹配，无需校验
        String col = column.strip();
        if (allowed.contains(col)) return;
        // 多表兼容：去掉表前缀再匹配（如 "id" ↔ "u.id"）
        String colPure = stripTablePrefix(col);
        if (allowed.stream().noneMatch(a -> stripTablePrefix(a).equalsIgnoreCase(colPure)))
            throw new IllegalArgumentException("非法列名: \"" + column + "\"，允许: " + allowed);
    }

    /**
     * 去除列名的表别名前缀（如 t.id → id，u.name → name）。
     * 无前缀时原样返回。
     */
    public static String stripTablePrefix(String column) {
        if (column == null) return "";
        int dot = column.lastIndexOf('.');
        return dot >= 0 ? column.substring(dot + 1) : column;
    }

    /**
     * 驼峰转下划线，用于前端 camelCase 参数与 SQL snake_case 列名统一匹配。
     * 支持表前缀：S.createTime → S.create_time，表别名不转换。
     * <pre>{@code
     * createTime  → create_time
     * id          → id
     * S.prodNum   → S.prod_num
     * }</pre>
     */
    public static String toUnderScoreCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) return camelCase;
        // 分离表前缀（如 S.column → prefix=S, colName=column）
        int dot = camelCase.lastIndexOf('.');
        if (dot >= 0) {
            String prefix = camelCase.substring(0, dot + 1); // 含点，如 "S."
            return prefix + doToUnderScore(camelCase.substring(dot + 1));
        }
        return doToUnderScore(camelCase);
    }

    private static String doToUnderScore(String camelCase) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');  // 首字母大写不加下划线
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
