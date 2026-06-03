package com.example.util.excel;

import com.example.template.util.FilterParam.ColumnItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Excel 导出上下文 — 预加载 remote-select 列的选项映射，整批复用（value → label）。
 *
 * <p>与 {@link ImportContext} 对称，但映射方向相反：导出时将数据库存储的 value 转换为用户可读的 label。
 *
 * <p>核心流程：
 * <ol>
 *   <li>收集 columns 中 fieldType=remote-select 的 optionType</li>
 *   <li>对每个 optionType 调 optionLoader(type, 2000) 全量加载</li>
 *   <li>通过反射从加载结果中提取 label/value（兼容任意 POJO）</li>
 *   <li>构建 value→label 映射（反向）</li>
 * </ol>
 */
@Slf4j
public class ExportContext {

    /**
     * optionType → (value → label) 映射
     */
    private final Map<String, Map<String, String>> valueToLabel;

    private ExportContext(List<ColumnItem> columns, BiFunction<String, Integer, List<?>> optionLoader) {
        this.valueToLabel = new HashMap<>();

        for (ColumnItem col : columns) {
            if (!"remote-select".equals(col.getFieldType())) continue;
            String optType = col.getOptionType();
            if (optType == null || optType.isEmpty()) continue;
            if (valueToLabel.containsKey(optType)) continue; // 相同 optionType 只加载一次

            List<?> options = optionLoader.apply(optType, 2000);
            Map<String, String> v2l = new HashMap<>();
            for (Object opt : options) {
                BeanWrapperImpl bw = new BeanWrapperImpl(opt);
                String label = (String) bw.getPropertyValue("label");
                String value = (String) bw.getPropertyValue("value");
                if (value != null) v2l.put(value, label);
            }
            valueToLabel.put(optType, v2l);
            log.info("ExportContext 预加载 optionType={}, 共 {} 条选项", optType, options.size());
        }
    }

    /**
     * 构建导出上下文
     *
     * @throws IllegalArgumentException 如果 columns 为 null 或空
     */
    public static ExportContext build(List<ColumnItem> columns, BiFunction<String, Integer, List<?>> optionLoader) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("columns 参数不能为空");
        }
        return new ExportContext(columns, optionLoader);
    }

    /**
     * 对 remote-select 列将数据库 value 转换为用户可读的 label：
     * <ul>
     *   <li>如果 rawValue 已有对应 label → 转换为 label 返回</li>
     *   <li>没有匹配 → 保留原 value</li>
     * </ul>
     *
     * @param col     对应的 ColumnItem
     * @param rawValue 数据库存储的 value
     * @return 转换后的 label，或原值（无匹配时）
     */
    public String convertValueToLabel(ColumnItem col, String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) return rawValue;

        String optType = col.getOptionType();
        if (optType == null || optType.isEmpty()) return rawValue;

        Map<String, String> v2l = valueToLabel.get(optType);
        if (v2l == null) {
            log.warn("ExportContext 中未找到 optionType={} 的选项数据，原样返回", optType);
            return rawValue;
        }

        String label = v2l.get(rawValue);
        if (label != null) {
            return label;
        }

        // 无匹配：保留原值
        return rawValue;
    }
}
