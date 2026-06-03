package com.example.service;

import com.example.domain.dto.ReportQueryResult;
import com.example.domain.model.ReportDefinition;
import java.util.List;
import java.util.Map;

/**
 * 报告服务 — 定义 + 查询执行
 */
public interface ReportService {

    /** 报告列表 */
    List<ReportDefinition> list();

    /** 获取报告定义 */
    ReportDefinition getById(String reportId);

    /** 保存报告定义（UPSERT 到 sys_config 表） */
    void save(ReportDefinition def);

    /** 删除报告（软删除） */
    void delete(String reportId);

    /**
     * 执行报告查询
     * @param reportId 报告ID
     * @param params   用户填写的参数（key → value）
     * @param current  页码
     * @param size     每页大小
     */
    ReportQueryResult query(String reportId, Map<String, Object> params, int current, int size);

    /**
     * 即时执行 SQL（不保存报告定义）
     * @param def     临时报告定义（含 SQL 模板 + 参数定义）
     * @param params  用户填写的参数值
     * @param current 页码
     * @param size    每页大小
     */
    ReportQueryResult execute(ReportDefinition def, Map<String, Object> params, int current, int size);
}
