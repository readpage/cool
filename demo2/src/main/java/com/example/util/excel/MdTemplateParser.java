package com.example.util.excel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MD 表格模板解析器 — 将 MD 表格字符串解析为结构数据。
 *
 * <pre>{@code
 * String md = """
 *     |姓名|年龄|性别|电话|
 *     |----|----|----|----|
 *     |张三| 25 | 男 |13800138000|""";
 * Result r = MdTemplateParser.parse(md);  // headers=[姓名,年龄,性别,电话], exampleRow=[张三,25,男,13800138000]
 * }</pre>
 */
public class MdTemplateParser {

    public record Result(List<String> headers, List<String> exampleRow) {}

    /**
     * 解析 MD 表格：第1行→表头，第2行→跳过，第3行→示例数据
     */
    public static Result parse(String md) {
        if (md == null || md.isBlank()) {
            return new Result(Collections.emptyList(), Collections.emptyList());
        }

        String[] lines = md.trim().split("\\n");
        List<String> headers = Collections.emptyList();
        List<String> exampleRow = Collections.emptyList();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isBlank()) continue;
            List<String> cells = splitCells(line);

            if (i == 0) {
                headers = cells;
            } else if (line.startsWith("|---") || line.startsWith("| --")) {
                continue;
            } else {
                exampleRow = cells;
                break;
            }
        }

        return new Result(headers, exampleRow);
    }

    private static List<String> splitCells(String line) {
        return Arrays.stream(line.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
