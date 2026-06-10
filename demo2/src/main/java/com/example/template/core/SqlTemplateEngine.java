package com.example.template.core;

import com.example.template.util.ColumnExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL 模板引擎核心 —— 解析自定义 DSL 并渲染为参数化 SQL。
 *
 * <h3>语法</h3>
 * <ul>
 *   <li>{{key}} — 字符串插值（从 params 取值直拼 SQL，用于 filter/sort 等系统片段）</li>
 *   <li>#{key}  — 参数化占位符（输出 :key，值由 JDBC 绑定，天然防注入）</li>
 *   <li>[[...]] — 可选块（内部任一 #{key} 有值时保留，否则丢弃）</li>
 * </ul>
 */
public class SqlTemplateEngine {

    private final Map<String, List<Segment>> cache = new ConcurrentHashMap<>();

    // ==================================================
    //  模板 → AST
    // ==================================================

    public List<Segment> compile(String template) {
        return cache.computeIfAbsent(template, this::doParse);
    }

    private List<Segment> doParse(String template) {
        List<Segment> segments = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int i = 0, len = template.length();

        while (i < len) {
            if (template.startsWith("{{", i)) {
                flushText(text, segments);
                int end = template.indexOf("}}", i + 2);
                if (end == -1) { text.append("{{"); i += 2; }
                else {
                    segments.add(Segment.interpolation(template.substring(i + 2, end).strip()));
                    i = end + 2;
                }
            } else if (template.startsWith("#{", i)) {
                flushText(text, segments);
                int end = template.indexOf("}", i + 2);
                if (end == -1) { text.append("#{"); i += 2; }
                else {
                    segments.add(Segment.param(template.substring(i + 2, end).strip()));
                    i = end + 1;
                }
            } else if (template.startsWith("[[", i)) {
                flushText(text, segments);
                int end = template.indexOf("]]", i + 2);
                if (end == -1) { text.append("[["); i += 2; }
                else {
                    segments.add(Segment.optional(doParse(template.substring(i + 2, end))));
                    i = end + 2;
                }
            } else {
                text.append(template.charAt(i));
                i++;
            }
        }
        flushText(text, segments);
        return segments;
    }

    private void flushText(StringBuilder text, List<Segment> segments) {
        if (text.length() > 0) { segments.add(Segment.text(text.toString())); text.setLength(0); }
    }

    // ==================================================
    //  列提取
    // ==================================================

    public List<String> extractColumns(String template) {
        return ColumnExtractor.extract(template);
    }

    // ==================================================
    //  AST → 参数化 SQL
    // ==================================================

    public SqlResult render(String template, Map<String, Object> params) {
        List<Segment> segments = compile(template);
        RenderContext ctx = new RenderContext(params);
        renderSegments(segments, ctx);
        return new SqlResult(ctx.sql.toString().trim());
    }

    private void renderSegments(List<Segment> segments, RenderContext ctx) {
        for (Segment seg : segments) {
            switch (seg.type) {
                case TEXT -> ctx.sql.append(seg.value);
                case INTERPOLATION -> {
                    Object val = ctx.params.get(seg.value);
                    ctx.sql.append(val != null ? val.toString() : "");
                }
                case PARAM -> ctx.sql.append(':').append(seg.value);
                case OPTIONAL -> {
                    if (hasAnyParamValue(seg.children, ctx.params))
                        renderSegments(seg.children, ctx);
                }
            }
        }
    }

    private boolean hasAnyParamValue(List<Segment> segments, Map<String, Object> params) {
        boolean foundParam = false;
        for (Segment seg : segments) {
            if (seg.type == SegmentType.PARAM) {
                foundParam = true;
                Object val = params.get(seg.value);
                if (val != null && !val.toString().isEmpty()) return true;
            } else if (seg.type == SegmentType.OPTIONAL) {
                if (hasAnyParamValue(seg.children, params)) return true;
            }
        }
        return false;
    }

    // ==================================================
    //  内部类型
    // ==================================================

    public static class Segment {
        final SegmentType type;
        final String value;
        final List<Segment> children;

        private Segment(SegmentType type, String value, List<Segment> children) {
            this.type = type; this.value = value; this.children = children;
        }
        static Segment text(String t)           { return new Segment(SegmentType.TEXT, t, List.of()); }
        static Segment interpolation(String k)  { return new Segment(SegmentType.INTERPOLATION, k, List.of()); }
        static Segment param(String k)          { return new Segment(SegmentType.PARAM, k, List.of()); }
        static Segment optional(List<Segment> c) { return new Segment(SegmentType.OPTIONAL, "", c); }
    }

    enum SegmentType { TEXT, INTERPOLATION, PARAM, OPTIONAL }

    private static class RenderContext {
        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> params;
        RenderContext(Map<String, Object> params) { this.params = params != null ? params : Collections.emptyMap(); }
    }
}
