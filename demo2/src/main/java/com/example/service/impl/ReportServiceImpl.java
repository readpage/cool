package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.domain.constant.ConfigConstants;
import com.example.domain.dto.ReportQueryResult;
import com.example.domain.entity.SysConfig;
import com.example.domain.model.ReportDefinition;
import com.example.mapper.SysConfigMapper;
import com.example.service.ReportService;
import com.example.service.SysConfigService;
import com.example.template.core.SqlResult;
import com.example.template.core.SqlTemplateEngine;
import com.example.template.QueryProperties;
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
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private static final String CONFIG_GROUP = "report";

    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private SysConfigMapper sysConfigMapper;
    @Resource
    private NamedParameterJdbcTemplate jdbc;
    @Resource
    private QueryProperties properties;

    private final SqlTemplateEngine engine = new SqlTemplateEngine();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ReportDefinition> list() {
        List<SysConfig> configs = sysConfigMapper.selectList(
            new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigGroup, CONFIG_GROUP)
                .eq(SysConfig::getDeleted, 0)
                .orderByDesc(SysConfig::getUpdateTime)
        );
        return configs.stream().map(c -> {
            try {
                ReportDefinition def = objectMapper.readValue(c.getConfigValue(), ReportDefinition.class);
                if (def.getId() == null) def.setId(c.getConfigKey());
                return def;
            } catch (JsonProcessingException e) {
                log.error("解析报告定义失败: {}", c.getConfigKey(), e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public ReportDefinition getById(String reportId) {
        SysConfig config = sysConfigService.getUserConfig(
                ConfigConstants.SYSTEM_USER_ID, CONFIG_GROUP, reportId);
        if (config == null || config.getConfigValue() == null) return null;
        try {
            ReportDefinition def = objectMapper.readValue(config.getConfigValue(), ReportDefinition.class);
            if (def.getId() == null) def.setId(reportId);
            return def;
        } catch (JsonProcessingException e) {
            log.error("解析报告定义失败: {}", reportId, e);
            return null;
        }
    }

    @Override
    public void save(ReportDefinition def) {
        try {
            String value = objectMapper.writeValueAsString(def);
            SysConfig config = new SysConfig();
            config.setConfigGroup(CONFIG_GROUP);
            config.setConfigKey(def.getId());
            config.setUserId(ConfigConstants.SYSTEM_USER_ID);
            config.setConfigValue(value);
            config.setVersion(0);
            config.setDeleted(0);
            sysConfigService.upsert(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化报告失败: " + def.getId(), e);
        }
    }

    @Override
    public void delete(String reportId) {
        SysConfig config = sysConfigService.getUserConfig(
                ConfigConstants.SYSTEM_USER_ID, CONFIG_GROUP, reportId);
        if (config != null) {
            config.setDeleted(1);
            sysConfigService.upsert(config);
        }
    }

    @Override
    public ReportQueryResult query(String reportId, Map<String, Object> params, int current, int size) {
        ReportDefinition def = getById(reportId);
        if (def == null) throw new RuntimeException("报告不存在: " + reportId);
        return executeQuery(def, params, current, size);
    }

    @Override
    public ReportQueryResult execute(ReportDefinition def, Map<String, Object> params, int current, int size) {
        return executeQuery(def, params, current, size);
    }

    /**
     * 核心方法：渲染 SQL 模板 → 执行查询 → 自动提取列元数据
     */
    public ReportQueryResult executeQuery(ReportDefinition def, Map<String, Object> params,
                                           int current, int size) {
        // 1. 渲染 SQL 模板（#{key} → :key，[[...]] 可选块）
        SqlResult result = engine.render(def.getSqlTemplate(), params);
        String renderedSql = result.getSql().trim();

        if (properties.isLogging()) {
            log.info("==> 报告 [{}] 渲染 SQL: {}", def.getId(), renderedSql);
            log.info("==> 参数: {}", params);
        }

        // 合并用户参数 + 报告定义的默认值
        Map<String, Object> mergedParams = new LinkedHashMap<>(params);
        if (def.getParameters() != null) {
            for (var p : def.getParameters()) {
                if (!mergedParams.containsKey(p.getName()) && p.getDefaultValue() != null) {
                    mergedParams.put(p.getName(), p.getDefaultValue());
                }
            }
        }

        // 2. COUNT（移除 ORDER BY 避免子查询报错）
        String countSql = renderedSql.replaceAll("(?i)\\s+ORDER\\s+BY\\s+.*$", "");
        countSql = "SELECT COUNT(*) FROM (" + countSql + ") AS _cnt";
        if (properties.isLogging()) {
            log.info("==> COUNT: {}", countSql);
        }
        Long total = jdbc.queryForObject(countSql, mergedParams, Long.class);

        // 3. 分页 SQL
        String limitSql;
        if (renderedSql.toUpperCase().contains("LIMIT") ||
            renderedSql.toUpperCase().contains("ROWNUM") ||
            renderedSql.toUpperCase().contains("FETCH")) {
            limitSql = renderedSql;
        } else {
            limitSql = renderedSql + " LIMIT " + size + " OFFSET " + ((current - 1) * size);
        }
        if (properties.isLogging()) {
            log.info("==> QUERY: {}", limitSql);
        }

        // 4. 执行查询 + 提取列元数据
        return jdbc.query(limitSql, mergedParams, (ResultSetExtractor<ReportQueryResult>) rs -> {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // ★ 用 col_0, col_1, ... 做 prop，getColumnLabel() 做 label
            List<ReportQueryResult.ColumnMeta> columns = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                columns.add(new ReportQueryResult.ColumnMeta(
                    "col_" + (i - 1),                // prop
                    meta.getColumnLabel(i),           // label = 别名 || 原始列名
                    meta.getColumnType(i)             // sqlType
                ));
            }

            // 读取数据行
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put("col_" + (i - 1), rs.getObject(i));
                }
                list.add(row);
            }

            return new ReportQueryResult(
                columns, list,
                total != null ? total : 0,
                current, size
            );
        });
    }
}
