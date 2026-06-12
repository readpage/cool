package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dao.ReportDao;
import com.example.domain.dto.ReportParam;
import com.example.domain.dto.ReportPermissionDto;
import com.example.domain.dto.ReportQueryResult;
import com.example.domain.dto.ReportSaveRequest;
import com.example.domain.dto.ReportSummary;
import com.example.domain.entity.Datasource;
import com.example.domain.entity.Report;
import com.example.domain.entity.SysConfig;
import com.example.domain.entity.UserConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.mapper.ReportMapper;
import com.example.service.DatasourceService;
import com.example.service.ReportService;
import com.example.service.SysConfigService;
import com.example.template.core.SqlResult;
import com.example.template.core.SqlTemplateEngine;
import com.example.template.datasource.DynamicJdbcFactory;
import com.example.template.QueryProperties;
import com.example.template.util.ColumnExtractor;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;
import com.example.template.util.SqlTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 报告服务实现
 *
 * <pre>
 *   架构：
 *   - report 表：存储元信息 + sqlTemplate + 权限 + table_key（业务唯一标识）
 *   - sys_config (configGroup='report', configKey=table_key)：系统默认展示配置（filter/sort/tableConfig）
 *   - user_config (configGroup='report', configKey=table_key)：用户个性化展示配置
 * </pre>
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private static final String DISPLAY_GROUP = "report";

    @Resource
    private ReportMapper reportMapper;
    @Resource
    private ReportDao reportDao;
    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private SqlTemplate sqlTemplate;
    @Resource
    private NamedParameterJdbcTemplate jdbc;
    @Resource
    private QueryProperties properties;
    @Resource
    private DynamicJdbcFactory dynamicJdbcFactory;
    @Resource
    private DatasourceService datasourceService;

    private final SqlTemplateEngine engine = new SqlTemplateEngine();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== CRUD ====================

    /**
     * 按 table_key 查找 report
     */
    private Report findByTableKey(String tableKey) {
        List<Report> list = reportMapper.selectList(
            new LambdaQueryWrapper<Report>().eq(Report::getTableKey, tableKey)
        );
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ReportSaveRequest> list() {
        List<Report> reports = reportMapper.selectList(
            new LambdaQueryWrapper<Report>()
                .orderByDesc(Report::getUpdateTime)
        );
        return reports.stream().map(this::toDefinition).collect(Collectors.toList());
    }

    @Override
    public List<ReportSummary> listSummary(Long userId, List<Integer> userRoleIds) {
        String roleIdsJson = buildRoleIdsJson(userRoleIds);
        List<Report> reports = reportDao.listAccessibleReports(userId, roleIdsJson);
        return reports.stream().map(r -> {
            ReportSummary s = new ReportSummary();
            s.setId(r.getId());
            s.setTableKey(r.getTableKey());
            s.setName(r.getName());
            s.setDescription(r.getDescription());
            s.setCategory(r.getCategory());
            s.setDisplayType(r.getDisplayType());
            s.setUpdateTime(r.getUpdateTime());
            // 填充数据源名称
            if (r.getDatasourceId() != null) {
                try {
                    Datasource ds = datasourceService.getById(r.getDatasourceId());
                    s.setDatasourceName(ds != null ? ds.getName() : null);
                } catch (Exception e) {
                    log.warn("获取数据源名称失败: datasourceId={}", r.getDatasourceId(), e);
                }
            }
            return s;
        }).collect(Collectors.toList());
    }

    @Override
    public ReportSaveRequest getByTableKey(String tableKey) {
        Report report = findByTableKey(tableKey);
        if (report == null) return null;
        return toDefinition(report);
    }

    @Override
    public void save(ReportSaveRequest req) {
        Report report = req.getReport();

        // 1) 保存/更新 report 表 — 按 id 判断新增还是更新
        Report exist = null;
        if (report.getId() != null) {
            exist = reportMapper.selectById(report.getId());
        }
        // 兜底：id 为空时按 table_key 查
        if (exist == null && report.getTableKey() != null) {
            exist = findByTableKey(report.getTableKey());
        }

        if (exist != null) {
            // 更新：保持 id 不变
            report.setId(exist.getId());
            reportMapper.updateById(report);
        } else {
            // 新增时 tableKey 为空则自动生成（基于自增ID回填）
            boolean autoGenTableKey = report.getTableKey() == null || report.getTableKey().isBlank();
            if (autoGenTableKey) {
                report.setTableKey("rpt_pending");
            }
            reportMapper.insert(report);
            if (autoGenTableKey) {
                report.setTableKey("rpt_" + report.getId());
                reportMapper.updateById(report);
            }
        }

        // 2) 保存系统默认展示配置到 sys_config（configKey = table_key）
        if (req.getDisplayConfig() != null) {
            String tableKey = report.getTableKey();
            try {
                String value = objectMapper.writeValueAsString(req.getDisplayConfig());
                SysConfig config = new SysConfig();
                config.setConfigGroup(DISPLAY_GROUP);
                config.setConfigKey(tableKey);
                config.setConfigValue(value);
                config.setVersion(0);
                sysConfigService.upsert(config);

                // 3) 清理所有用户的缓存配置，强制下次访问走 copy-on-read 获取最新版本
                UserConfig condition = new UserConfig();
                condition.setConfigGroup(DISPLAY_GROUP);
                condition.setConfigKey(tableKey);
                sqlTemplate.deleteByKeys(condition, "configGroup", "configKey");
            } catch (JsonProcessingException e) {
                throw new RuntimeException("序列化展示配置失败: " + tableKey, e);
            }
        }
    }

    @Override
    public void deleteByTableKey(String tableKey) {
        Report report = findByTableKey(tableKey);
        if (report == null) return;

        reportMapper.deleteById(report.getId());

        // 删除关联的 sys_config
        SysConfig sysConfig = sysConfigService.getSystemConfig(DISPLAY_GROUP, tableKey);
        if (sysConfig != null) {
            sysConfigService.removeById(sysConfig.getId());
        }
        // 清理该报告所有用户的 user_config（按 configGroup + config_key 删除）
        UserConfig condition = new UserConfig();
        condition.setConfigGroup(DISPLAY_GROUP);
        condition.setConfigKey(tableKey);
        sqlTemplate.deleteByKeys(condition, "configGroup", "configKey");
    }

    // ==================== 查询执行 ====================

    @Override
    public ReportQueryResult queryByTableKey(String tableKey, FilterParam param,
                                              Long userId, List<Integer> userRoleIds) {
        Report report = findByTableKey(tableKey);
        if (report == null) throw new RuntimeException("报告不存在: " + tableKey);

        if (!hasAccess(report, userId, userRoleIds)) {
            throw new RuntimeException("无权访问该报告: " + tableKey);
        }

        NamedParameterJdbcTemplate targetJdbc = resolveJdbc(report.getDatasourceId());
        return executeQuery(report.getSqlTemplate(), param, targetJdbc);
    }

    @Override
    public ReportQueryResult queryByTableKeyInternal(String tableKey, FilterParam param) {
        Report report = findByTableKey(tableKey);
        if (report == null) throw new RuntimeException("报告不存在: " + tableKey);
        NamedParameterJdbcTemplate targetJdbc = resolveJdbc(report.getDatasourceId());
        return executeQuery(report.getSqlTemplate(), param, targetJdbc);
    }

    @Override
    public ReportQueryResult execute(String sqlTemplate, FilterParam param) {
        Long datasourceId = null;
        if (param instanceof ReportParam rp) {
            datasourceId = rp.getDatasourceId();
        }
        NamedParameterJdbcTemplate targetJdbc = resolveJdbc(datasourceId);
        return executeQuery(sqlTemplate, param, targetJdbc);
    }

    // ==================== 私有方法 ====================

    /**
     * Report 实体 → ReportSaveRequest（组合 report 实体 + 展示配置）
     * configKey 使用 report.table_key
     */
    private ReportSaveRequest toDefinition(Report report) {
        ReportSaveRequest result = new ReportSaveRequest();
        result.setReport(report);

        // 关联数据源名称（冗余返回，免去前端全量拉取数据源列表）
        if (report.getDatasourceId() != null) {
            try {
                Datasource ds = datasourceService.getById(report.getDatasourceId());
                result.setDatasourceName(ds != null ? ds.getName() : null);
            } catch (Exception e) {
                log.warn("获取数据源名称失败: datasourceId={}", report.getDatasourceId(), e);
            }
        }

        // 加载系统默认展示配置（configKey = table_key）
        SysConfig sc = sysConfigService.getSystemConfig(DISPLAY_GROUP, report.getTableKey());
        if (sc != null && sc.getConfigValue() != null) {
            try {
                JsonNode dc = objectMapper.readTree(sc.getConfigValue());
                result.setDisplayConfig(dc);
            } catch (JsonProcessingException e) {
                log.error("解析展示配置失败: tableKey={}", report.getTableKey(), e);
            }
        }
        return result;
    }
    /**
     * 核心方法：两路分支
     * <ul>
     *   <li>模板含 {{filter}}/{{sort}}：委托 SqlTemplate 执行（filter/sort 嵌入 SQL）</li>
     *   <li>模板不含占位符：仅渲染 #{key}/[[...]] + 分页，不追加任何 filter/sort</li>
     * </ul>
     */
    private ReportQueryResult executeQuery(String sql, FilterParam param, NamedParameterJdbcTemplate targetJdbc) {
        boolean hasPlaceholder = sql.contains("{{filter}}") || sql.contains("{{sort}}");
        if (hasPlaceholder) {
            return executeWithPlaceholder(sql, param, targetJdbc);
        }
        return executePlainSql(sql, param, targetJdbc);
    }

    /** 有占位符路径 — 用目标数据源的 JdbcTemplate 创建 SqlTemplate 执行 */
    private ReportQueryResult executeWithPlaceholder(String sql, FilterParam param, NamedParameterJdbcTemplate targetJdbc) {
        SqlTemplate st = new SqlTemplate(targetJdbc, properties);
        PageResult<Map<String, Object>> page = st.pageForMap(sql, param);
        List<String> extractedProps  = ColumnExtractor.lastExtract(sql);
        List<String> extractedLabels = ColumnExtractor.extractAliases(sql);
        ColumnBuildResult colResult = buildColumnsAndRemap(page.getList(), extractedProps, extractedLabels);
        return new ReportQueryResult(colResult.columns, colResult.data,
                page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 无占位符路径 — 仅渲染 #{key}/[[...]] + 分页，用目标数据源的 JdbcTemplate */
    private ReportQueryResult executePlainSql(String sql, FilterParam param, NamedParameterJdbcTemplate targetJdbc) {
        // 渲染 #{key}/[[...]]（不应用 filter/sort，引擎仅处理参数化占位符）
        FilterParam.DaoResult dr = param.startPage().buildForDao(sql);
        Map<String, Object> rawParams = dr.toFlatMap();
        SqlResult sqlResult = engine.render(sql, rawParams);
        String renderedSql = sqlResult.getSql().trim();

        int current = param.getCurrent() > 0 ? param.getCurrent() : 1;
        int size    = param.getSize()    > 0 ? param.getSize()    : 10;

        // 清理悬空的 WHERE / ORDER BY（filter/sort 为空时 {{key}} 渲染为空字符串）
        renderedSql = renderedSql.replaceAll("(?i)\\s+WHERE\\s*$", "").trim();
        renderedSql = renderedSql.replaceAll("(?i)\\s+ORDER\\s+BY\\s*$", "").trim();

        // COUNT
        String countSql = "SELECT COUNT(*) FROM (" + renderedSql + ") AS _cnt";
        if (properties.isLogging()) log.info("==> COUNT: {}", countSql);
        Long total = targetJdbc.queryForObject(countSql, rawParams, Long.class);

        // 分页数据（根据数据库类型自适应 LIMIT/OFFSET、FETCH 或 ROWNUM）
        String pagedSql = buildLimitSql(renderedSql, size, current, targetJdbc);
        if (properties.isLogging()) log.info("==> SQL: {}", pagedSql);

        List<String> lastExtractProps = ColumnExtractor.lastExtract(sql);
        List<String> extractAliases   = ColumnExtractor.extractAliases(sql);

        return targetJdbc.query(pagedSql, rawParams, (ResultSetExtractor<ReportQueryResult>) rs -> {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            Set<String> seen = new LinkedHashSet<>();
            List<ReportQueryResult.ColumnMeta> columns = new ArrayList<>();
            List<String> props = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                String jdbcLabel = meta.getColumnLabel(i);
                String prop  = (i - 1 < lastExtractProps.size()) ? lastExtractProps.get(i - 1) : jdbcLabel;
                String label = (i - 1 < extractAliases.size())   ? extractAliases.get(i - 1)   : jdbcLabel;
                String dedupProp = prop;
                int counter = 1;
                while (seen.contains(dedupProp)) {
                    dedupProp = prop + "_" + (++counter);
                }
                seen.add(dedupProp);
                props.add(dedupProp);
                columns.add(new ReportQueryResult.ColumnMeta(dedupProp, label, meta.getColumnType(i)));
            }
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(props.get(i - 1), rs.getObject(i));
                }
                list.add(row);
            }
            return new ReportQueryResult(columns, list, total != null ? total : 0, current, size);
        });
    }

    /**
     * 根据 datasourceId 解析目标 JdbcTemplate。
     * NULL → 主数据源；非 NULL → 从 DynamicJdbcFactory 获取。
     * 应用重启后缓存丢失时，自动从 DB 加载数据源实体并懒初始化连接池。
     */
    private NamedParameterJdbcTemplate resolveJdbc(Long datasourceId) {
        if (datasourceId == null) return jdbc;
        NamedParameterJdbcTemplate target = dynamicJdbcFactory.getJdbcTemplate(datasourceId);
        if (target == null) {
            // 缓存未命中（应用重启后）→ 从 DB 加载数据源并创建连接池
            Datasource ds = datasourceService.getById(datasourceId);
            if (ds != null) {
                target = dynamicJdbcFactory.getOrCreate(ds);
            }
        }
        if (target == null) {
            throw new RuntimeException("数据源连接池未初始化，请先保存数据源配置: id=" + datasourceId);
        }
        return target;
    }

    /**
     * 根据数据库类型生成分页 SQL（MySQL / PostgreSQL / SQLServer / Oracle 自适应）。
     */
    private String buildLimitSql(String renderedSql, int size, int currentPage, NamedParameterJdbcTemplate targetJdbc) {
        String dbType = detectDbType(targetJdbc);
        int offset = (currentPage - 1) * size;
        if ("sqlserver".equals(dbType)) {
            if (!renderedSql.toUpperCase().contains("ORDER BY")) {
                renderedSql += " ORDER BY (SELECT 0)";
            }
            return renderedSql + " OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY";
        }
        if ("oracle".equals(dbType)) {
            return "SELECT * FROM (SELECT _ora_.*, ROWNUM _rn_ FROM (" + renderedSql
                    + ") _ora_ WHERE ROWNUM <= " + (offset + size) + ") WHERE _rn_ > " + offset;
        }
        return renderedSql + " LIMIT " + size + " OFFSET " + offset;
    }

    /** 数据库类型缓存（DataSource → mysql|sqlserver|oracle|postgresql） */
    private static final Map<String, String> DB_TYPE_CACHE = new ConcurrentHashMap<>();

    /** 从 JDBC URL 检测数据库类型 */
    private String detectDbType(NamedParameterJdbcTemplate targetJdbc) {
        DataSource ds = targetJdbc.getJdbcTemplate().getDataSource();
        if (ds == null) return "mysql";
        String key = ds.toString();
        return DB_TYPE_CACHE.computeIfAbsent(key, k -> {
            try (Connection conn = ds.getConnection()) {
                String url = conn.getMetaData().getURL();
                if (url != null) {
                    String lowerUrl = url.toLowerCase();
                    if (lowerUrl.contains(":sqlserver:") || lowerUrl.contains(":jtds:")) return "sqlserver";
                    if (lowerUrl.contains(":oracle:")) return "oracle";
                    if (lowerUrl.contains(":postgresql:")) return "postgresql";
                }
            } catch (SQLException e) {
                log.warn("无法检测数据库类型，默认使用 MySQL 语法", e);
            }
            return "mysql";
        });
    }

    /**
     * 从 queryForList 返回的数据构建列元数据 + 重映射行 key（JDBC label → ColumnExtractor prop）。
     * prop 来自 lastExtract，label 来自 extractAliases，两路路径取值统一。
     */
    private ColumnBuildResult buildColumnsAndRemap(List<Map<String, Object>> data,
                                                   List<String> extractedProps,
                                                   List<String> extractedLabels) {
        Set<String> seen = new LinkedHashSet<>();
        List<ReportQueryResult.ColumnMeta> columns = new ArrayList<>();
        List<String> dedupProps = new ArrayList<>();

        if (data != null && !data.isEmpty()) {
            List<String> jdbcLabels = new ArrayList<>(data.get(0).keySet());
            for (int i = 0; i < jdbcLabels.size(); i++) {
                String prop  = (i < extractedProps.size())  ? extractedProps.get(i)  : jdbcLabels.get(i);
                String label = (i < extractedLabels.size()) ? extractedLabels.get(i) : jdbcLabels.get(i);
                String dedupProp = prop;
                int counter = 1;
                while (seen.contains(dedupProp)) {
                    dedupProp = prop + "_" + (++counter);
                }
                seen.add(dedupProp);
                dedupProps.add(dedupProp);
                columns.add(new ReportQueryResult.ColumnMeta(dedupProp, label, 0));
            }
            // 重映射每行的 key：JDBC label → dedupProp
            for (Map<String, Object> row : data) {
                Map<String, Object> remapped = new LinkedHashMap<>();
                for (int i = 0; i < jdbcLabels.size(); i++) {
                    remapped.put(dedupProps.get(i), row.get(jdbcLabels.get(i)));
                }
                row.clear();
                row.putAll(remapped);
            }
        } else {
            // 无数据时用 ColumnExtractor 结果构建空列元数据
            for (int i = 0; i < extractedProps.size(); i++) {
                String prop  = extractedProps.get(i);
                String label = i < extractedLabels.size() ? extractedLabels.get(i) : prop;
                String dedupProp = prop;
                int counter = 1;
                while (seen.contains(dedupProp)) {
                    dedupProp = prop + "_" + (++counter);
                }
                seen.add(dedupProp);
                dedupProps.add(dedupProp);
                columns.add(new ReportQueryResult.ColumnMeta(dedupProp, label, 0));
            }
        }
        return new ColumnBuildResult(columns, data != null ? data : List.of());
    }

    private record ColumnBuildResult(List<ReportQueryResult.ColumnMeta> columns, List<Map<String, Object>> data) {}

    // ==================== 导出（全量查询，不分页） ====================

    @Override
    public List<Map<String, Object>> queryAllByTableKey(String tableKey, FilterParam param) {
        Report report = findByTableKey(tableKey);
        if (report == null) throw new RuntimeException("报告不存在: " + tableKey);
        param.setCurrent(1);
        param.setSize(Integer.MAX_VALUE);
        NamedParameterJdbcTemplate targetJdbc = resolveJdbc(report.getDatasourceId());
        return executeQuery(report.getSqlTemplate(), param, targetJdbc).getList();
    }

    @Override
    public List<Map<String, Object>> executeForExport(String sqlTemplate, FilterParam param) {
        param.setCurrent(1);
        param.setSize(Integer.MAX_VALUE);
        Long datasourceId = null;
        if (param instanceof ReportParam rp) {
            datasourceId = rp.getDatasourceId();
        }
        NamedParameterJdbcTemplate targetJdbc = resolveJdbc(datasourceId);
        return executeQuery(sqlTemplate, param, targetJdbc).getList();
    }

    @Override
    public long countByDatasourceId(Long datasourceId) {
        if (datasourceId == null) return 0;
        return reportMapper.selectCount(
            new LambdaQueryWrapper<Report>().eq(Report::getDatasourceId, datasourceId)
        );
    }

    // ==================== 权限管理 ====================

    @Override
    public ReportPermissionDto getPermission(String tableKey) {
        Report report = findByTableKey(tableKey);
        if (report == null) throw new RuntimeException("报告不存在: " + tableKey);

        if (report.getPermissionConfig() == null || report.getPermissionConfig().isBlank()) {
            return new ReportPermissionDto();
        }
        try {
            return objectMapper.readValue(report.getPermissionConfig(), ReportPermissionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("权限配置解析失败: " + tableKey, e);
        }
    }

    @Override
    public void updatePermission(String tableKey, ReportPermissionDto dto) {
        Report report = findByTableKey(tableKey);
        if (report == null) throw new RuntimeException("报告不存在: " + tableKey);
        try {
            report.setPermissionConfig(objectMapper.writeValueAsString(dto));
            reportMapper.updateById(report);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("权限配置序列化失败: " + tableKey, e);
        }
    }

    // ==================== 权限校验 ====================

    /**
     * 判断当前用户是否有权访问指定报表。
     * <ul>
     *   <li>创建者始终有权限</li>
     *   <li>permissionConfig.roleIds 与 userRoleIds 有交集 → 放行</li>
     *   <li>否则拒绝</li>
     * </ul>
     */
    private boolean hasAccess(Report report, Long userId, List<Integer> userRoleIds) {
        // 创建者始终有权访问
        if (userId != null && userId.equals(report.getCreatorId())) {
            return true;
        }
        // 无权限配置 → 拒绝（非创建者）
        if (report.getPermissionConfig() == null || report.getPermissionConfig().isBlank()) {
            return false;
        }
        try {
            ReportPermissionDto perm = objectMapper.readValue(
                report.getPermissionConfig(), ReportPermissionDto.class);

            if (perm.getRoleIds() != null && !perm.getRoleIds().isEmpty()
                    && userRoleIds != null) {
                return userRoleIds.stream().anyMatch(id -> perm.getRoleIds().contains(id));
            }
            return false;
        } catch (JsonProcessingException e) {
            log.error("解析权限配置失败: tableKey={}", report.getTableKey(), e);
            return false;
        }
    }

    /** 将角色 ID 列表转为 JSON 数组字符串，供 JSON_OVERLAPS 使用 */
    private String buildRoleIdsJson(List<Integer> userRoleIds) {
        if (userRoleIds == null || userRoleIds.isEmpty()) {
            return "[]";
        }
        return "[" + userRoleIds.stream().map(String::valueOf)
            .collect(Collectors.joining(",")) + "]";
    }
}
