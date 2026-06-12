package com.example.service;

import com.example.domain.dto.ReportPermissionDto;
import com.example.domain.dto.ReportQueryResult;
import com.example.domain.dto.ReportSaveRequest;
import com.example.domain.dto.ReportSummary;
import com.example.domain.entity.Role;
import com.example.template.util.FilterParam;

import java.util.List;
import java.util.Map;

/**
 * 报告服务接口
 */
public interface ReportService {

    /** 报告列表（不含用户个性化展示配置） */
    List<ReportSaveRequest> list();

    /** 报告摘要列表（不含敏感信息，用于侧边栏展示） */
    List<ReportSummary> listSummary(Long userId, List<Integer> userRoleIds);

    /** 获取报告定义（含系统默认展示配置） */
    ReportSaveRequest getByTableKey(String tableKey);

    /** 保存报告定义（含展示配置） */
    void save(ReportSaveRequest req);

    /** 删除报告（含关联的展示配置） */
    void deleteByTableKey(String tableKey);

    /** 执行已保存报告的查询（含权限校验） */
    ReportQueryResult queryByTableKey(String tableKey, FilterParam param, Long userId, List<Integer> userRoleIds);

    /** 执行已保存报告的查询（内部调用，不校验权限，用于选项下拉等场景） */
    ReportQueryResult queryByTableKeyInternal(String tableKey, FilterParam param);

    /** 即时执行 SQL 查询（不依赖已保存的报告） */
    ReportQueryResult execute(String sqlTemplate, FilterParam param);

    /** 导出已保存报告 — 全量查询（不分页） */
    List<Map<String, Object>> queryAllByTableKey(String tableKey, FilterParam param);

    /** 导出即时 SQL — 全量查询（不分页） */
    List<Map<String, Object>> executeForExport(String sqlTemplate, FilterParam param);

    /** 统计引用指定数据源的报告数量 */
    long countByDatasourceId(Long datasourceId);

    // ==================== 权限管理 ====================

    /** 获取报表权限配置 */
    ReportPermissionDto getPermission(String tableKey);

    /** 更新报表权限配置 */
    void updatePermission(String tableKey, ReportPermissionDto dto);
}
