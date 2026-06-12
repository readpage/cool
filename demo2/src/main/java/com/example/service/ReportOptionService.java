package com.example.service;

import com.example.domain.dto.ReportQueryResult;
import com.example.domain.dto.ReportSaveRequest;
import com.example.domain.entity.Option;
import com.example.template.util.ColumnExtractor;
import com.example.template.util.FilterParam;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报告选项服务 — 通过报告的 SQL 模板执行获取选项列表。
 * <p>
 * 约定：报告 SQL 必须包含 value 和 label 两列别名：
 * <pre>{@code
 * SELECT id AS value, name AS label
 * FROM sys_user
 * WHERE deleted = 0
 *   AND {{filter}}
 * {{sort}}
 * }</pre>
 * 注意：{{sort}} 渲染时自带 ORDER BY 关键字，模板中不要额外写 ORDER BY。
 *
 * @author readpage
 */
@Slf4j
@Service
public class ReportOptionService {

    @Resource
    private ReportService reportService;

    /**
     * 通过报告 tableKey 执行 SQL 获取选项列表。
     *
     * @param tableKey 报告唯一标识（对应 options 表中的 type 以 "rpt:" 前缀传入）
     * @param keyword  搜索关键字，对 label 列做模糊匹配（可为 null）
     * @param limit    返回条数上限
     * @return 选项列表（value + label）
     */
    public List<Option> getOptionsFromReport(String tableKey, String keyword, int limit) {
        // 1. 获取报告定义 → 拿到 SQL 模板
        ReportSaveRequest def = reportService.getByTableKey(tableKey);
        if (def == null || def.getReport() == null) {
            throw new RuntimeException("报告不存在: " + tableKey);
        }
        String sqlTemplate = def.getReport().getSqlTemplate();

        // 2. 从 SQL 模板提取列映射（prop ↔ alias），找到 label 对应的 prop（用于 filter 白名单校验）
        List<String> props = ColumnExtractor.lastExtract(sqlTemplate);
        List<String> aliases = ColumnExtractor.extractAliases(sqlTemplate);

        String labelProp = null;
        for (int i = 0; i < Math.min(props.size(), aliases.size()); i++) {
            if ("label".equalsIgnoreCase(aliases.get(i))) {
                labelProp = props.get(i);
                break;
            }
        }

        // 3. 构造 FilterParam
        FilterParam param = new FilterParam();
        param.setSize(Math.min(limit, 50));
        param.setCurrent(1);

        if (keyword != null && !keyword.isBlank() && labelProp != null) {
            // ⚠️ filter column 必须用 prop 名（SQL 中 AS 前面的名称），以通过 ColumnExtractor 白名单校验
            param.setFilter(List.of(
                new FilterParam.FilterItem()
                    .setColumn(labelProp)
                    .setOperator("contains")
                    .setValue(keyword)
            ));
        }

        // 4. 执行报告 SQL
        ReportQueryResult result = reportService.queryByTableKeyInternal(tableKey, param);

        // 5. 从列元数据找到 value/label 对应的 prop
        //    buildColumnsAndRemap 后：col.prop=AS 前原名, col.label=AS 后别名
        //    行数据的 key 也是 prop（原名），因此需要通过 label 找到 prop 来取值
        String valueProp = null;
        String labelResultProp = null;
        for (ReportQueryResult.ColumnMeta col : result.getColumns()) {
            if ("value".equalsIgnoreCase(col.getLabel())) {
                valueProp = col.getProp();
            } else if ("label".equalsIgnoreCase(col.getLabel())) {
                labelResultProp = col.getProp();
            }
        }

        if (valueProp == null || labelResultProp == null) {
            throw new RuntimeException(
                "报告 SQL 必须包含 value 和 label 两列别名（如 SELECT id AS value, name AS label ...）: " + tableKey);
        }

        // 6. 映射为 Option 列表
        List<Option> options = new ArrayList<>();
        for (Map<String, Object> row : result.getList()) {
            Option opt = new Option();
            opt.setValue(String.valueOf(row.get(valueProp)));
            opt.setLabel(String.valueOf(row.get(labelResultProp)));
            options.add(opt);
        }
        return options;
    }
}
