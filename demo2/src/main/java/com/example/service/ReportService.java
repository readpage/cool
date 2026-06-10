package com.example.service;

import com.example.domain.dto.ReportQueryResult;
import com.example.domain.dto.ReportSaveRequest;
import com.example.template.util.FilterParam;

import java.util.List;
import java.util.Map;

/**
 * 报告服务接口
 */
public interface ReportService {

    /** 报告列表（不含用户个性化展示配置） */
    List<ReportSaveRequest> list();

    /** 获取报告定义（含系统默认展示配置） */
    ReportSaveRequest getByTableKey(String tableKey);

    /** 获取报告定义（copy-on-read：无 user_config 时从 sys_config 复制一份） */
    ReportSaveRequest getByTableKey(String tableKey, Long userId);

    /** 保存报告定义（含展示配置） */
    void save(ReportSaveRequest req);

    /** 删除报告（含关联的展示配置） */
    void deleteByTableKey(String tableKey);

    /** 执行已保存报告的查询 */
    ReportQueryResult queryByTableKey(String tableKey, FilterParam param);

    /** 即时执行 SQL 查询（不依赖已保存的报告） */
    ReportQueryResult execute(String sqlTemplate, FilterParam param);

    /** 导出已保存报告 — 全量查询（不分页） */
    List<Map<String, Object>> queryAllByTableKey(String tableKey, FilterParam param);

    /** 导出即时 SQL — 全量查询（不分页） */
    List<Map<String, Object>> executeForExport(String sqlTemplate, FilterParam param);

    /** 统计引用指定数据源的报告数量 */
    long countByDatasourceId(Long datasourceId);
}
