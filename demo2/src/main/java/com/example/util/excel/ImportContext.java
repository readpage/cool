package com.example.util.excel;

import com.example.template.util.FilterParam.ColumnItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Excel 导入上下文 — 预加载 remote-select 列的选项映射，整批复用。
 *
 * <p>核心流程：
 * <ol>
 *   <li>收集 columns 中 fieldType=remote-select 的 optionType</li>
 *   <li>对每个 optionType 调 optionLoader(type, 2000) 全量加载</li>
 *   <li>通过反射从加载结果中提取 label/value（兼容任意 POJO）</li>
 *   <li>构建 label→value 映射 + allValues 集合（用于判断单元格值是否已是有效 value）</li>
 * </ol>
 */
@Slf4j
public class ImportContext {

    /** 哨兵对象：宽松模式下标记该字段跳过（不覆盖实体默认值） */
    public static final String SKIP_MARKER = "__SKIP__";

    /** label → ColumnItem（用于表头匹配） */
    private final Map<String, ColumnItem> colByLabel;

    /**
     * optionType → (label → value) 映射
     */
    private final Map<String, Map<String, String>> labelToValue;

    /**
     * optionType → 所有有效 value 集合（用于判断单元格值是否已是有效的 value）
     */
    private final Map<String, Set<String>> allValues;

    private ImportContext(List<ColumnItem> columns, BiFunction<String, Integer, List<?>> optionLoader) {
        this.colByLabel = columns.stream()
                .collect(Collectors.toMap(ColumnItem::getLabel, c -> c, (a, b) -> a));

        this.labelToValue = new HashMap<>();
        this.allValues = new HashMap<>();

        // 预加载 remote-select 列的选项
        for (ColumnItem col : columns) {
            if (!"remote-select".equals(col.getFieldType())) continue;
            String optType = col.getOptionType();
            if (optType == null || optType.isEmpty()) continue;
            if (labelToValue.containsKey(optType)) continue; // 相同 optionType 只加载一次

            List<?> options = optionLoader.apply(optType, 2000);
            Map<String, String> l2v = new HashMap<>();
            Set<String> values = new HashSet<>();
            for (Object opt : options) {
                BeanWrapperImpl bw = new BeanWrapperImpl(opt);
                String label = (String) bw.getPropertyValue("label");
                String value = (String) bw.getPropertyValue("value");
                if (label != null) l2v.put(label, value);
                if (value != null) values.add(value);
            }
            labelToValue.put(optType, l2v);
            allValues.put(optType, values);
            log.info("ImportContext 预加载 optionType={}, 共 {} 条选项", optType, options.size());
        }
    }

    /**
     * 构建导入上下文
     *
     * @throws IllegalArgumentException 如果 columns 为 null 或空
     */
    public static ImportContext build(List<ColumnItem> columns, BiFunction<String, Integer, List<?>> optionLoader) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("columns 参数不能为空");
        }
        return new ImportContext(columns, optionLoader);
    }

    /**
     * 根据表头 label 获取 ColumnItem
     */
    public ColumnItem getByLabel(String label) {
        return colByLabel.get(label);
    }

    /**
     * 对 remote-select 列转换单元格值：
     * <ul>
     *   <li>如果 rawValue 已是有效 value → 直接返回</li>
     *   <li>如果是 label → 转换为 value 返回</li>
     *   <li>都不匹配 → 记录 warning 并返回 SKIP_MARKER（宽松模式，跳过该字段）</li>
     * </ul>
     *
     * @param col      对应的 ColumnItem
     * @param rawValue  单元格原始值（trim 后）
     * @return 转换后的 value，或 SKIP_MARKER 表示跳过
     */
    public String convertCellValue(ColumnItem col, String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) return rawValue;

        String optType = col.getOptionType();
        if (optType == null || optType.isEmpty()) return rawValue;

        Set<String> values = allValues.get(optType);
        Map<String, String> l2v = labelToValue.get(optType);

        if (values == null || l2v == null) {
            log.warn("ImportContext 中未找到 optionType={} 的选项数据，原样返回", optType);
            return rawValue;
        }

        // 已是有效 value，无需转换
        if (values.contains(rawValue)) {
            return rawValue;
        }

        // 尝试 label → value 转换
        String converted = l2v.get(rawValue);
        if (converted != null) {
            log.debug("label→value: \"{}\" → \"{}\" (optionType={})", rawValue, converted, optType);
            return converted;
        }

        // 都不匹配：宽松模式，跳过该字段
        log.warn("列 [{}] 的值 \"{}\" 不是有效 value 也不是可识别的 label (optionType={})，跳过该字段",
                col.getLabel(), rawValue, optType);
        return SKIP_MARKER;
    }
}
