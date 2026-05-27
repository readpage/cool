package com.example.template.util;

import com.example.template.QueryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SQL 模板执行器 — 只需 SQL 模板 + FilterParam，内部自动校验列白名单 + 生成参数 + 执行。
 *
 * <pre>{@code
 * @Autowired
 * private SqlTemplate sqlTemplate;
 *
 * List<User> users = sqlTemplate.query(
 *     "SELECT id, username FROM user WHERE {{filter}} {{sort}}",
 *     param, User.class);
 * }</pre>
 */
@Component
public class SqlTemplate {

    private static final Logger log = LoggerFactory.getLogger(SqlTemplate.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final QueryProperties properties;

    public SqlTemplate(NamedParameterJdbcTemplate jdbc, QueryProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    /**
     * 执行动态 SQL 模板。
     *
     * @param sql        SQL 模板（可用 {{filter}} / {{sort}} 占位符）
     * @param param      FilterParam 筛选 + 排序参数
     * @param resultType 返回实体类型
     * @param <T>        实体类型
     * @return 结果列表
     */
    public <T> List<T> query(String sql, FilterParam param, Class<T> resultType) {
        FilterParam.DaoResult r = param.buildForDao(sql);
        String finalSql = sql
                .replace("{{filter}}", r.filter())
                .replace("{{sort}}",   r.sort());
        if (properties.isLogging()) {
            log.info("==>  SQL : {}", finalSql);
            log.info("==> PARAMS: {}", r.params());
        }
        return jdbc.query(finalSql, r.params(), new BeanPropertyRowMapper<>(resultType));
    }

    /**
     * 分页查询 — 自动 COUNT + LIMIT。
     *
     * @param sql        SQL 模板（可用 {{filter}} / {{sort}} 占位符）
     * @param param      FilterParam（需包含 current/size 分页字段）
     * @param resultType 返回实体类型
     * @param <T>        实体类型
     * @return 分页结果
     */
    public <T> PageResult<T> page(String sql, FilterParam param, Class<T> resultType) {
        FilterParam.DaoResult r = param.buildForDao(sql);
        String finalSql = sql
                .replace("{{filter}}", r.filter())
                .replace("{{sort}}",   r.sort());

        int current = param.getCurrent() > 0 ? param.getCurrent() : 1;
        int size    = param.getSize()    > 0 ? param.getSize()    : 10;

        // 1. COUNT
        String countSql = buildCountSql(finalSql);
        if (properties.isLogging()) {
            log.info("==> COUNT : {}", countSql);
        }
        Long total = jdbc.queryForObject(countSql, r.params(), Long.class);

        // 2. LIMIT
        String limitSql = finalSql + " LIMIT " + size + " OFFSET " + (current - 1) * size;
        if (properties.isLogging()) {
            log.info("==>  SQL : {}", limitSql);
            log.info("==> PARAMS: {}", r.params());
        }
        List<T> records = jdbc.query(limitSql, r.params(), new BeanPropertyRowMapper<>(resultType));

        return new PageResult<>(total != null ? total : 0, current, size, records);
    }

    /** 从 SQL 截取 COUNT：移除 ORDER BY，SELECT ... FROM → SELECT COUNT(*) FROM */
    private String buildCountSql(String renderedSql) {
        String sql = renderedSql.replaceAll("(?i)\\s+ORDER\\s+BY\\s+.*$", "");
        return sql.replaceAll("(?i)^\\s*SELECT\\s+.+?\\s+FROM\\s+", "SELECT COUNT(*) FROM ");
    }
}
