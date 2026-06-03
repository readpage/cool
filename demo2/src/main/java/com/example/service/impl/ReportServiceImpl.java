package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.domain.dto.ReportQueryResult;
import com.example.domain.dto.ReportSaveRequest;
import com.example.domain.entity.Report;
import com.example.domain.entity.SysConfig;
import com.example.domain.entity.UserConfig;
import com.example.domain.model.ReportDisplayConfig;
import com.example.mapper.ReportMapper;
import com.example.service.ReportService;
import com.example.service.SysConfigService;
import com.example.service.UserConfigService;
import com.example.template.core.SqlResult;
import com.example.template.core.SqlTemplateEngine;
import com.example.template.QueryProperties;
import com.example.template.util.FilterParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报告服务实现
 *
 * <pre>
 *   架构：
 *   - report 表：存储元信息 + sqlTemplate + 权限 + table_key（业务唯一标识）
 *   - sys_config (configGroup='report_display', configKey=table_key)：系统默认展示配置（filter/sort/tableConfig）
 *   - user_config (configGroup='report_display', configKey=table_key)：用户个性化展示配置
 * </pre>
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private static final String DISPLAY_GROUP = "report_display";

    // 默认用户 ID（后续接入认证后替换为当前登录用户）
    private static final Long DEFAULT_USER_ID = 1L;

    @Resource
    private ReportMapper reportMapper;
    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private UserConfigService userConfigService;
    @Resource
    private NamedParameterJdbcTemplate jdbc;
    @Resource
    private QueryProperties properties;

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
    public ReportSaveRequest getByTableKey(String tableKey) {
        Report report = findByTableKey(tableKey);
        if (report == null) return null;
        return toDefinition(report);
    }

    @Override
    public ReportSaveRequest getByTableKey(String tableKey, Long userId) {
        Report report = findByTableKey(tableKey);
        if (report == null) return null;
        ReportSaveRequest def = toDefinition(report);

        // 1) 优先查 user_config（最快路径，1 次查询）
        UserConfig uc = userConfigService.getUserConfig(userId, DISPLAY_GROUP, tableKey);
        if (uc != null && uc.getConfigValue() != null) {
            try {
                ReportDisplayConfig userConfig = objectMapper.readValue(uc.getConfigValue(), ReportDisplayConfig.class);
                def.setDisplayConfig(userConfig);
                return def;
            } catch (JsonProcessingException e) {
                log.error("解析用户展示配置失败: tableKey={} userId={}", tableKey, userId, e);
            }
        }

        // 2) 用户无配置，且系统默认存在 → copy-on-read：复制到 user_config
        ReportDisplayConfig sysDisplay = def.getDisplayConfig();
        if (sysDisplay != null) {
            try {
                UserConfig newUc = new UserConfig();
                newUc.setConfigGroup(DISPLAY_GROUP);
                newUc.setConfigKey(tableKey);
                newUc.setUserId(userId);
                newUc.setConfigValue(objectMapper.writeValueAsString(sysDisplay));
                userConfigService.upsert(newUc);
            } catch (JsonProcessingException e) {
                log.error("copy-on-read 写入 user_config 失败: tableKey={} userId={}", tableKey, userId, e);
            }
        }

        return def;
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
            reportMapper.insert(report);
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
        // 清理该报告所有用户的 user_config（按 configGroup + table_key 删除）
        List<UserConfig> allUserConfigs = userConfigService.list(
            new LambdaQueryWrapper<UserConfig>()
                .eq(UserConfig::getConfigGroup, DISPLAY_GROUP)
                .eq(UserConfig::getConfigKey, tableKey)
        );
        if (allUserConfigs != null) {
            allUserConfigs.forEach(uc -> userConfigService.removeById(uc.getId()));
        }
    }

    // ==================== 查询执行 ====================

    @Override
    public ReportQueryResult queryByTableKey(String tableKey, FilterParam param) {
        Report report = findByTableKey(tableKey);
        if (report == null) throw new RuntimeException("报告不存在: " + tableKey);
        return executeQuery(report.getSqlTemplate(), param);
    }

    @Override
    public ReportQueryResult execute(String sqlTemplate, FilterParam param) {
        return executeQuery(sqlTemplate, param);
    }

    // ==================== 私有方法 ====================

    /**
     * Report 实体 → ReportSaveRequest（组合 report 实体 + 展示配置）
     * configKey 使用 report.table_key
     */
    private ReportSaveRequest toDefinition(Report report) {
        ReportSaveRequest result = new ReportSaveRequest();
        result.setReport(report);

        // 加载系统默认展示配置（configKey = table_key）
        SysConfig sc = sysConfigService.getSystemConfig(DISPLAY_GROUP, report.getTableKey());
        if (sc != null && sc.getConfigValue() != null) {
            try {
                ReportDisplayConfig dc = objectMapper.readValue(sc.getConfigValue(), ReportDisplayConfig.class);
                result.setDisplayConfig(dc);
            } catch (JsonProcessingException e) {
                log.error("解析展示配置失败: tableKey={}", report.getTableKey(), e);
            }
        }
        return result;
    }
    /**
     * 核心方法：两路分支处理
     * <ul>
     *   <li>模板含 {{filter}}/{{sort}}：占位符替换模式</li>
     *   <li>模板不含 {{filter}}/{{sort}}：子查询包裹模式</li>
     * </ul>
     */
    private ReportQueryResult executeQuery(String sqlTemplate, FilterParam param) {
        boolean hasFilterPlaceholder = sqlTemplate.contains("{{filter}}");
        boolean hasSortPlaceholder = sqlTemplate.contains("{{sort}}");

        // 1. 生成 filter/sort SQL 片段 + 命名参数
        FilterParam.DaoResult dr = param.buildForDao(sqlTemplate);
        String filterSql = dr.filter();
        String sortSql = dr.sort();
        Map<String, Object> namedParams = new LinkedHashMap<>(dr.params());

        // 2. 渲染 SQL 模板
        Map<String, Object> renderParams = dr.toFlatMap();
        SqlResult sqlResult = engine.render(sqlTemplate, renderParams);
        String renderedSql = sqlResult.getSql().trim();

        if (properties.isLogging()) {
            log.info("==> 报告渲染 SQL: {}", renderedSql);
            log.info("==> 筛选: {}", filterSql);
            log.info("==> 排序: {}", sortSql);
        }

        // 3. 构建基础 SQL
        String baseSql;
        if (hasFilterPlaceholder || hasSortPlaceholder) {
            baseSql = renderedSql;
        } else {
            baseSql = renderedSql.replaceAll("(?i)\\s+ORDER\\s+BY\\s+.*$", "").trim();
            baseSql = baseSql.replaceAll("(?i)\\s+LIMIT\\s+\\d+(\\s+OFFSET\\s+\\d+)?\\s*$", "").trim();
        }

        // 4. COUNT
        String countSql;
        if (hasFilterPlaceholder) {
            countSql = "SELECT COUNT(*) FROM (" + baseSql + ") AS _cnt";
        } else if (!filterSql.isEmpty()) {
            countSql = "SELECT COUNT(*) FROM (" + baseSql + ") AS _cnt " + filterSql;
        } else {
            countSql = "SELECT COUNT(*) FROM (" + baseSql + ") AS _cnt";
        }
        if (properties.isLogging()) log.info("==> COUNT: {}", countSql);
        Long total = jdbc.queryForObject(countSql, namedParams, Long.class);

        // 5. 数据查询 SQL
        String querySql;
        if (hasFilterPlaceholder || hasSortPlaceholder) {
            querySql = renderedSql;
        } else if (!filterSql.isEmpty() || !sortSql.isEmpty()) {
            querySql = "SELECT * FROM (" + baseSql + ") AS _report " + filterSql + " " + sortSql;
        } else {
            querySql = baseSql;
        }
        querySql = querySql + " LIMIT " + param.getSize() + " OFFSET " + ((param.getCurrent() - 1) * param.getSize());
        if (properties.isLogging()) log.info("==> QUERY: {}", querySql);

        // 6. 执行
        int current = param.getCurrent();
        int size = param.getSize();
        return jdbc.query(querySql, namedParams, (ResultSetExtractor<ReportQueryResult>) rs -> {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            List<ReportQueryResult.ColumnMeta> columns = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                columns.add(new ReportQueryResult.ColumnMeta(
                    "col_" + (i - 1),
                    meta.getColumnLabel(i),
                    meta.getColumnType(i)
                ));
            }

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put("col_" + (i - 1), rs.getObject(i));
                }
                list.add(row);
            }

            return new ReportQueryResult(columns, list, total != null ? total : 0, current, size);
        });
    }
}
