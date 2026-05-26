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
}
