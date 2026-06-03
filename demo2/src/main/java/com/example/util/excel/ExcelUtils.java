package com.example.util.excel;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.PageReadListener;
import com.example.template.util.FilterParam;
import com.example.template.util.FilterParam.ColumnItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.util.excel.handler.DynamicCellAlignHandler;
import com.example.util.excel.handler.DynamicColumnWidthHandler;
import com.example.util.excel.handler.HeaderStyleHandler;
import com.example.util.excel.model.ColumnExportConfig;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Excel 导入导出工具类
 */
@Slf4j
public class ExcelUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 导入 ====================

    public static <T> List<T> read(InputStream inputStream, Class<T> clazz, int headRowIndex) {
        List<T> list = new ArrayList<>();
        EasyExcel.read(inputStream)
                .head(clazz)
                .sheet()
                .registerReadListener(new PageReadListener<T>(list::addAll))
                .headRowNumber(headRowIndex)
                .doRead();
        return list;
    }

    public static <T> List<T> read(MultipartFile file, Class<T> clazz) {
        try {
            return read(file.getInputStream(), clazz, 1);
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 失败", e);
        }
    }

    public static <T> List<T> read(MultipartFile file, Class<T> clazz, int headRowIndex) {
        try {
            return read(file.getInputStream(), clazz, headRowIndex);
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 失败", e);
        }
    }

    // ==================== 原始读取（无实体模型，返回 List<List<String>>） ====================

    /**
     * 不绑定实体模型，将 Excel 完整读为 List&lt;List&lt;String&gt;&gt;。
     * 第 0 行 = 表头，后续 = 数据行。每个单元格 trim 后存入。
     */
    @SuppressWarnings("unchecked")
    public static List<List<String>> readRaw(InputStream inputStream) {
        List<Object> rawRows = EasyExcel.read(inputStream).sheet().headRowNumber(0).doReadSync();
        List<List<String>> result = new ArrayList<>();
        for (Object obj : rawRows) {
            Map<Integer, String> row = (Map<Integer, String>) obj;
            List<String> cells = new ArrayList<>();
            for (int i = 0; ; i++) {
                if (!row.containsKey(i)) break;
                String val = row.get(i);
                cells.add(val != null ? val.trim() : "");
            }
            result.add(cells);
        }
        return result;
    }

    public static List<List<String>> readRaw(MultipartFile file) {
        try {
            return readRaw(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 失败", e);
        }
    }

    // ==================== 动态列导入 ====================

    /**
     * 根据 FilterParam.columns 动态导入（一步到位）：
     * <ol>
     *   <li>校验 columns 并构建 ImportContext（预加载 remote-select 映射）</li>
     *   <li>读原始行（表头 + 数据）</li>
     *   <li>表头 label 匹配 ColumnItem</li>
     *   <li>remote-select 列通过 ImportContext 转换 label→value</li>
     *   <li>反射设值，SKIP_MARKER 的字段跳过（保留实体默认值）</li>
     * </ol>
     */
    public static <T> List<T> importData(MultipartFile file, List<ColumnItem> columns,
                                         BiFunction<String, Integer, List<?>> optionLoader, Class<T> clazz) {
        ImportContext ctx = ImportContext.build(columns, optionLoader);
        return importData(file, ctx, clazz);
    }

    /**
     * 根据 filterParam JSON 字符串动态导入，内部完成 JSON 解析。
     *
     * @throws IllegalArgumentException 如果 filterParamJson 解析失败
     */
    public static <T> List<T> importData(MultipartFile file, String filterParamJson,
                                         BiFunction<String, Integer, List<?>> optionLoader, Class<T> clazz) {
        FilterParam param;
        try {
            param = objectMapper.readValue(filterParamJson, FilterParam.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("filterParam JSON 解析失败：" + e.getMessage(), e);
        }
        return importData(file, param.getColumns(), optionLoader, clazz);
    }

    /**
     * 根据 ImportContext 动态导入（适用于已有 ImportContext 的场景）
     */
    public static <T> List<T> importData(MultipartFile file, ImportContext ctx, Class<T> clazz) {
        try {
            return importData(file.getInputStream(), ctx, clazz);
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 失败", e);
        }
    }

    private static <T> List<T> importData(InputStream inputStream, ImportContext ctx, Class<T> clazz) {
        List<List<String>> allRows = readRaw(inputStream);
        if (allRows.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> headers = allRows.get(0);

        // 表头索引 → ColumnItem
        ColumnItem[] colMapping = new ColumnItem[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            colMapping[i] = ctx.getByLabel(headers.get(i));
        }

        List<T> entities = new ArrayList<>();
        for (int r = 1; r < allRows.size(); r++) {
            List<String> row = allRows.get(r);
            T entity;
            try {
                entity = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.warn("第 {} 行：无法创建实体实例，跳过该行", r + 1);
                continue;
            }

            BeanWrapperImpl bw = new BeanWrapperImpl(entity);
            for (int c = 0; c < colMapping.length && c < row.size(); c++) {
                ColumnItem colItem = colMapping[c];
                if (colItem == null || colItem.getProp() == null) continue;

                String rawValue = row.get(c);
                if (rawValue == null || rawValue.isEmpty()) continue;

                // remote-select 列：label → value 转换
                String converted = ctx.convertCellValue(colItem, rawValue);
                if (ImportContext.SKIP_MARKER.equals(converted)) {
                    continue; // 宽松模式：跳过该字段，保留实体默认值
                }

                try {
                    bw.setPropertyValue(colItem.getProp(), convertTypedValue(colItem.getProp(), converted, clazz));
                } catch (Exception e) {
                    log.warn("第 {} 行列 [{}] 设置属性 {} 失败: {}",
                            r + 1, headers.get(c), colItem.getProp(), e.getMessage());
                }
            }

            entities.add(entity);
        }

        log.info("导入完成：共 {} 行数据，成功解析 {} 条实体", allRows.size() - 1, entities.size());
        return entities;
    }

    /**
     * 根据目标字段类型转换字符串值。
     * BeanWrapperImpl 自带类型转换，但 remote-select 转换后已是 String，需要针对 Integer/Long 等再转。
     */
    private static <T> Object convertTypedValue(String prop, String value, Class<T> clazz) {
        try {
            java.lang.reflect.Field field = findField(clazz, prop);
            if (field == null) return value;

            Class<?> type = field.getType();
            if (type == Integer.class || type == int.class) {
                return Integer.valueOf(value);
            } else if (type == Long.class || type == long.class) {
                return Long.valueOf(value);
            } else if (type == Double.class || type == double.class) {
                return Double.valueOf(value);
            }
            // String / 其他直接返回
            return value;
        } catch (NumberFormatException e) {
            log.warn("属性 {} 值 \"{}\" 无法转为数字，保持原始字符串", prop, value);
            return value;
        }
    }

    private static java.lang.reflect.Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    // ==================== 导出 ====================

    /**
     * FilterParam 动态列导出（无 label 转换）
     */
    public static <T> void export(HttpServletResponse response, String fileName,
                                   FilterParam param, List<T> dataList) throws IOException {
        List<List<String>> headers = extractHeaders(param);
        List<List<Object>> rows = extractRows(param, dataList);
        List<ColumnExportConfig> columnConfigs = toColumnConfigs(param);

        writeExport(response, fileName, headers, rows, columnConfigs);
    }

    /**
     * FilterParam 动态列导出（带 remote-select value→label 转换）
     *
     * @param optionLoader 选项加载器：type → limit → List，用于预加载 value→label 映射
     */
    public static <T> void export(HttpServletResponse response, String fileName,
                                   FilterParam param, List<T> dataList,
                                   BiFunction<String, Integer, List<?>> optionLoader) throws IOException {
        ExportContext ctx = ExportContext.build(param.getColumns(), optionLoader);
        List<List<String>> headers = extractHeaders(param);
        List<List<Object>> rows = extractRows(param, dataList, ctx);
        List<ColumnExportConfig> columnConfigs = toColumnConfigs(param);

        writeExport(response, fileName, headers, rows, columnConfigs);
    }

    private static void writeExport(HttpServletResponse response, String fileName,
                                     List<List<String>> headers, List<List<Object>> rows,
                                     List<ColumnExportConfig> columnConfigs) throws IOException {
        setResponseHeader(response, fileName);

        FastExcel.write(response.getOutputStream())
                .head(headers)
                .registerWriteHandler(new HeaderStyleHandler())
                .registerWriteHandler(new DynamicCellAlignHandler(columnConfigs))
                .registerWriteHandler(new DynamicColumnWidthHandler(columnConfigs))
                .sheet(!columnConfigs.isEmpty() ? "数据" : "Sheet")
                .doWrite(rows);
    }

    // ==================== 导入模板下载 ====================

    /**
     * 根据 MD 模板生成导入模板 Excel 供用户下载。
     * <p>第1行：表头（来自 MD 第1行）；第2行：示例数据（来自 MD 第3行）。
     * <p>列宽按表头字符数自动估算。
     */
    public static void exportImporterTemplate(HttpServletResponse response, String fileName,
                                               String mdTemplate) throws IOException {
        MdTemplateParser.Result result = MdTemplateParser.parse(mdTemplate);
        List<String> headers = result.headers();
        List<String> exampleRow = result.exampleRow();

        if (headers.isEmpty()) {
            throw new IllegalArgumentException("MD 模板表头不能为空");
        }

        // 按表头和示例数据中较长的字符数自动估算列宽
        List<ColumnExportConfig> columnConfigs = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            int hLen = headers.get(i).length();
            int dLen = i < exampleRow.size() ? exampleRow.get(i).length() : 0;
            int width = Math.max(Math.max(hLen, dLen) * 2 + 4, 10);
            columnConfigs.add(new ColumnExportConfig(null, width, null));
        }

        List<List<String>> excelHeaders = headers.stream()
                .map(List::of)
                .collect(Collectors.toList());

        List<List<Object>> dataRows = List.of(
                new ArrayList<>(exampleRow)
        );

        setResponseHeader(response, fileName);

        FastExcel.write(response.getOutputStream())
                .head(excelHeaders)
                .registerWriteHandler(new HeaderStyleHandler())
                .registerWriteHandler(new DynamicCellAlignHandler(columnConfigs))
                .registerWriteHandler(new DynamicColumnWidthHandler(columnConfigs))
                .sheet("导入模板")
                .doWrite(dataRows);
    }

    /**
     * 根据 ColumnItem 列表构建 MD 模板字符串。
     * <p>remote-select 列的示例值从 optionExamples（optionType → 示例 label）中取，
     * 调用方负责提前查询好示例数据传入，保持工具类与 Spring 解耦。
     */
    public static String buildMdTemplate(List<ColumnItem> columns,
                                          Map<String, String> optionExamples) {
        StringBuilder sb = new StringBuilder();

        // 表头行
        String headerLine = columns.stream()
                .map(c -> "|" + (c.getLabel() != null ? c.getLabel() : ""))
                .collect(Collectors.joining()) + "|";
        sb.append(headerLine).append("\n");

        // 分隔行
        String sepLine = columns.stream()
                .map(c -> "|----")
                .collect(Collectors.joining()) + "|";
        sb.append(sepLine).append("\n");

        // 示例数据行
        for (ColumnItem col : columns) {
            String example;
            if ("remote-select".equals(col.getFieldType()) && col.getOptionType() != null) {
                example = optionExamples.getOrDefault(col.getOptionType(), "");
            } else {
                example = col.getProp() != null ? col.getProp() : "";
            }
            sb.append("|").append(example);
        }
        sb.append("|");

        return sb.toString();
    }

    // ==================== 响应头 ====================

    private static void setResponseHeader(HttpServletResponse response, String fileName) {
        String encoded = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + encoded);
    }

    // ==================== FilterParam 辅助 ====================

    private static List<List<String>> extractHeaders(FilterParam param) {
        List<ColumnItem> columns = param.getColumns();
        if (columns == null || columns.isEmpty()) return Collections.emptyList();
        return columns.stream()
                .map(c -> Collections.singletonList(c.getLabel()))
                .collect(Collectors.toList());
    }

    private static <T> List<List<Object>> extractRows(FilterParam param, List<T> dataList) {
        List<ColumnItem> columns = param.getColumns();
        if (columns == null || columns.isEmpty()) return Collections.emptyList();
        return dataList.stream().map(item -> {
            BeanWrapperImpl bw = new BeanWrapperImpl(item);
            return columns.stream()
                    .map(c -> bw.getPropertyValue(c.getProp()))
                    .collect(Collectors.toList());
        }).collect(Collectors.toList());
    }

    /**
     * 带 value→label 转换的 extractRows（供导出时 remote-select 列使用）
     */
    private static <T> List<List<Object>> extractRows(FilterParam param, List<T> dataList, ExportContext ctx) {
        List<ColumnItem> columns = param.getColumns();
        if (columns == null || columns.isEmpty()) return Collections.emptyList();
        return dataList.stream().map(item -> {
            BeanWrapperImpl bw = new BeanWrapperImpl(item);
            return columns.stream()
                    .map(c -> {
                        Object raw = bw.getPropertyValue(c.getProp());
                        if (!"remote-select".equals(c.getFieldType()) || raw == null) {
                            return raw;
                        }
                        return ctx.convertValueToLabel(c, raw.toString());
                    })
                    .collect(Collectors.toList());
        }).collect(Collectors.toList());
    }

    private static List<ColumnExportConfig> toColumnConfigs(FilterParam param) {
        List<ColumnItem> columns = param.getColumns();
        if (columns == null) {
            log.warn("FilterParam.columns is null");
            return Collections.emptyList();
        }
        log.info("FilterParam.columns count={}", columns.size());
        columns.forEach(c -> log.info("  column prop={}, label={}, width={}, minWidth={}",
                c.getProp(), c.getLabel(), c.getWidth(), c.getMinWidth()));
        return columns.stream()
                .map(c -> new ColumnExportConfig(c.getAlign(), c.getWidth(), c.getMinWidth()))
                .collect(Collectors.toList());
    }
}
