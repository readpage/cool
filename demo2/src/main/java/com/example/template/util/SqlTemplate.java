package com.example.template.util;

import com.example.template.QueryProperties;
import com.example.template.core.SqlResult;
import com.example.template.core.SqlTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 *
 * <p>批量操作自动从实体类注解读取表名、主键和字段映射（@TableName / @TableId / @TableField）。</p>
 */
@Component
public class SqlTemplate {

    private static final Logger log = LoggerFactory.getLogger(SqlTemplate.class);

    /** 数据库类型缓存（DataSource → mysql|sqlserver|oracle|postgresql） */
    private static final Map<String, String> DB_TYPE_CACHE = new ConcurrentHashMap<>();

    private final NamedParameterJdbcTemplate jdbc;
    private final QueryProperties properties;
    private final SqlTemplateEngine engine;

    public SqlTemplate(NamedParameterJdbcTemplate jdbc, QueryProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
        this.engine = new SqlTemplateEngine();
    }

    // ==================== 查询 ====================

    /**
     * 执行动态 SQL 模板 — 支持完整模板语法：{{key}} / #{key} / [[...]]。
     *
     * @param sql        SQL 模板（可用 {{filter}} / {{sort}} 占位符，#{key} 参数化，[[...]] 可选块）
     * @param param      FilterParam 筛选 + 排序参数
     * @param resultType 返回实体类型
     * @param <T>        实体类型
     * @return 结果列表
     */
    public <T> List<T> query(String sql, FilterParam param, Class<T> resultType) {
        FilterParam.DaoResult r = param.buildForDao(sql);
        SqlResult rendered = engine.render(sql, r.toFlatMap());
        String renderedSql = FilterParam.cleanRenderedSql(rendered.getSql());
        if (properties.isLogging()) {
            log.info("==>  SQL : {}", renderedSql);
            log.info("==> PARAMS: {}", r.params());
        }
        return jdbc.query(renderedSql, r.params(), new BeanPropertyRowMapper<>(resultType));
    }

    /**
     * 分页查询 — 自动 COUNT + LIMIT，多数据库自适应。
     *
     * @param sql        SQL 模板（可用 {{filter}} / {{sort}} 占位符，#{key} 参数化，[[...]] 可选块）
     * @param param      FilterParam（需包含 current/size 分页字段）
     * @param resultType 返回实体类型
     * @param <T>        实体类型
     * @return 分页结果
     */
    public <T> PageResult<T> page(String sql, FilterParam param, Class<T> resultType) {
        FilterParam.DaoResult r = param.startPage().buildForDao(sql);
        SqlResult rendered = engine.render(sql, r.toFlatMap());
        String renderedSql = FilterParam.cleanRenderedSql(rendered.getSql());

        int current = param.getCurrent() > 0 ? param.getCurrent() : 1;
        int size    = param.getSize()    > 0 ? param.getSize()    : 10;

        // 1. COUNT
        String countSql = buildCountSql(renderedSql);
        long total = executeCount(countSql, r.params());

        // 2. 分页数据
        String limitSql = buildLimitSql(renderedSql, size, current);
        logSql(limitSql, r.params());
        List<T> records = jdbc.query(limitSql, r.params(), new BeanPropertyRowMapper<>(resultType));

        return new PageResult<>(total, current, size, records);
    }

    /**
     * 分页查询（返回原始 Map）— 适用于动态报告等无实体类的场景。
     *
     * @param sql   SQL 模板（可用 {{filter}} / {{sort}} 占位符，#{key} 参数化，[[...]] 可选块）
     * @param param FilterParam（需包含 current/size 分页字段）
     * @return 分页 Map 结果
     */
    public PageResult<Map<String, Object>> pageForMap(String sql, FilterParam param) {
        FilterParam.DaoResult r = param.startPage().buildForDao(sql);
        SqlResult rendered = engine.render(sql, r.toFlatMap());
        String renderedSql = FilterParam.cleanRenderedSql(rendered.getSql());

        int current = param.getCurrent() > 0 ? param.getCurrent() : 1;
        int size    = param.getSize()    > 0 ? param.getSize()    : 10;

        String countSql = buildCountSql(renderedSql);
        long total = executeCount(countSql, r.params());

        String limitSql = buildLimitSql(renderedSql, size, current);
        logSql(limitSql, r.params());
        List<Map<String, Object>> list = jdbc.queryForList(limitSql, r.params());

        return new PageResult<>(total, current, size, list);
    }

    // ==================== 插入 ====================

    /**
     * 单条插入 — 跳过 null 字段，依赖数据库默认值。
     *
     * <pre>{@code
     * User user = new User();
     * user.setUsername("tom");
     * user.setAge(20);
     * sqlTemplate.insert(user);   // INSERT INTO user (username, age) VALUES (:username, :age)
     * }</pre>
     */
    public <T> void insert(T entity) {
        batchInsert(Collections.singletonList(entity));
    }

    /**
     * 批量插入 — 跳过 null 字段，依赖数据库默认值。
     * <p>按非空字段集自动分组执行，每批最多 500 条。</p>
     */
    @SuppressWarnings("unchecked")
    public <T> void batchInsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) return;
        Class<T> entityClass = (Class<T>) entities.get(0).getClass();
        List<BatchSqlBuilder.BatchSql> batches = BatchSqlBuilder.buildInsert(entityClass, entities);
        executeBatches(batches);
    }

    // ==================== 删除 ====================

    /**
     * 按 key 单条删除。
     * <p>若 keyFields 为空，默认取 @TableId 主键字段。</p>
     *
     * <pre>{@code
     * sqlTemplate.deleteByKeys(user);                      // WHERE id = ?
     * sqlTemplate.deleteByKeys(config, "configGroup", "configKey");  // WHERE config_group = ? AND config_key = ?
     * }</pre>
     */
    public <T> void deleteByKeys(T entity, String... keyFields) {
        batchDeleteByKeys(Collections.singletonList(entity), keyFields);
    }

    /**
     * 按 key 批量删除。
     * <p>若 keyFields 为空，默认取 @TableId 主键字段。</p>
     *
     * <pre>{@code
     * sqlTemplate.batchDeleteByKeys(users);                      // WHERE id IN (...)
     * sqlTemplate.batchDeleteByKeys(configs, "configGroup", "configKey");
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public <T> void batchDeleteByKeys(List<T> entities, String... keyFields) {
        if (entities == null || entities.isEmpty()) return;
        Class<T> entityClass = (Class<T>) entities.get(0).getClass();
        List<String> keys = resolveKeyFields(entityClass, keyFields);
        List<BatchSqlBuilder.BatchSql> batches = BatchSqlBuilder.buildDelete(entityClass, entities, keys);
        executeBatches(batches);
    }

    /**
     * 按 ID 批量删除（快捷方法，ID 字段从 @TableId 注解获取）。
     *
     * <pre>{@code
     * sqlTemplate.batchDeleteByIds(User.class, List.of(1L, 2L, 3L));
     * }</pre>
     */
    public <T> void batchDeleteByIds(Class<T> entityClass, List<?> ids) {
        if (ids == null || ids.isEmpty()) return;
        String idField = EntityMeta.getIdField(entityClass);
        if (idField == null) {
            throw new IllegalArgumentException("实体类 " + entityClass.getName() + " 未标注 @TableId");
        }
        // 构造虚拟实体列表（只有主键有值）
        List<T> entities = new ArrayList<>();
        for (Object id : ids) {
            try {
                T entity = entityClass.getDeclaredConstructor().newInstance();
                java.lang.reflect.Field f = findField(entityClass, idField);
                if (f != null) {
                    f.setAccessible(true);
                    f.set(entity, id);
                }
                entities.add(entity);
            } catch (Exception e) {
                throw new RuntimeException("无法创建实体实例: " + entityClass.getName(), e);
            }
        }
        batchDeleteByKeys(entities);
    }

    // ==================== 更新 ====================

    /**
     * 按 key 单条更新 — 只更新非 null 字段。
     * <p>若 keyFields 为空，默认取 @TableId 主键字段。</p>
     *
     * <pre>{@code
     * user.setAge(30);
     * sqlTemplate.updateByKeys(user);                             // SET age = ? WHERE id = ?
     * sqlTemplate.updateByKeys(config, "configGroup", "configKey");
     * }</pre>
     */
    public <T> void updateByKeys(T entity, String... keyFields) {
        batchUpdateByKeys(Collections.singletonList(entity), keyFields);
    }

    /**
     * 按 key 批量更新 — 只更新非 null 字段。
     * <p>若 keyFields 为空，默认取 @TableId 主键字段。</p>
     *
     * <pre>{@code
     * sqlTemplate.batchUpdateByKeys(users);                          // 按 ID 更新
     * sqlTemplate.batchUpdateByKeys(configs, "configGroup", "configKey");
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public <T> void batchUpdateByKeys(List<T> entities, String... keyFields) {
        if (entities == null || entities.isEmpty()) return;
        Class<T> entityClass = (Class<T>) entities.get(0).getClass();
        List<String> keys = resolveKeyFields(entityClass, keyFields);
        List<BatchSqlBuilder.BatchSql> batches = BatchSqlBuilder.buildUpdate(entityClass, entities, keys);
        executeBatches(batches);
    }

    // ==================== UPSERT ====================

    /**
     * 单条保存或更新 — INSERT ... ON DUPLICATE KEY UPDATE（MySQL）。
     * <p>若 conflictKeys 为空，默认取 @TableId 主键字段。</p>
     *
     * <pre>{@code
     * sqlTemplate.saveOrUpdate(user);                                  // 按 ID 冲突更新
     * sqlTemplate.saveOrUpdate(config, "configGroup", "configKey");
     * }</pre>
     */
    public <T> void saveOrUpdate(T entity, String... conflictKeys) {
        batchSaveOrUpdate(Collections.singletonList(entity), conflictKeys);
    }

    /**
     * 批量保存或更新 — INSERT ... ON DUPLICATE KEY UPDATE（MySQL）。
     * <p>若 conflictKeys 为空，默认取 @TableId 主键字段。</p>
     *
     * <pre>{@code
     * sqlTemplate.batchSaveOrUpdate(users);                            // 按 ID 冲突更新
     * sqlTemplate.batchSaveOrUpdate(configs, "configGroup", "configKey");
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public <T> void batchSaveOrUpdate(List<T> entities, String... conflictKeys) {
        if (entities == null || entities.isEmpty()) return;
        Class<T> entityClass = (Class<T>) entities.get(0).getClass();
        List<String> keys = resolveKeyFields(entityClass, conflictKeys);
        List<BatchSqlBuilder.BatchSql> batches = BatchSqlBuilder.buildUpsert(entityClass, entities, keys);
        executeBatches(batches);
    }

    // ==================== 内部方法 ====================

    /** 执行分批 SQL */
    private void executeBatches(List<BatchSqlBuilder.BatchSql> batches) {
        for (BatchSqlBuilder.BatchSql b : batches) {
            if (properties.isLogging()) {
                log.info("==> BATCH SQL : {}", b.sql());
                log.info("==> BATCH SIZE: {}", b.params().length);
            }
            jdbc.batchUpdate(b.sql(), b.params());
        }
    }

    /**
     * 解析 key 字段列表：若未指定则从 @TableId 获取。
     * @throws IllegalArgumentException 如果既没有传 keyFields 也没有 @TableId
     */
    private static List<String> resolveKeyFields(Class<?> entityClass, String... keyFields) {
        if (keyFields != null && keyFields.length > 0) {
            List<String> list = new ArrayList<>();
            for (String kf : keyFields) {
                if (kf != null && !kf.isEmpty()) list.add(kf);
            }
            if (!list.isEmpty()) return list;
        }
        String id = EntityMeta.getIdField(entityClass);
        if (id == null) {
            throw new IllegalArgumentException(
                    "实体类 " + entityClass.getName() + " 未标注 @TableId，必须显式传入 keyFields");
        }
        return Collections.singletonList(id);
    }

    private static java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    /**
     * 从 SQL 截取 COUNT：移除 ORDER BY，SELECT ... FROM → SELECT COUNT(*) FROM。
     * 支持 CTE/WITH 查询 — 保留 CTE 部分，仅包装最终 SELECT。
     */
    private String buildCountSql(String renderedSql) {
        String sql = renderedSql.replaceAll("(?i)\\s+ORDER\\s+BY\\s+.*$", "");
        if (sql.trim().toUpperCase().startsWith("WITH ")) {
            Pattern p = Pattern.compile("\\)\\s*SELECT\\s", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(sql);
            int splitPos = -1;
            while (m.find()) {
                splitPos = m.start() + 1;
            }
            if (splitPos > 0) {
                String ctePart = sql.substring(0, splitPos);
                String finalSelect = sql.substring(splitPos);
                return ctePart + "SELECT COUNT(*) FROM (" + finalSelect + ") _count_sub";
            }
            return "SELECT COUNT(*) FROM (" + sql + ") _count_sub";
        }
        return "SELECT COUNT(*) FROM (" + sql + ") _count_sub";
    }

    /** 执行 COUNT 查询，统一处理 null → 0 */
    private long executeCount(String countSql, Map<String, Object> params) {
        if (properties.isLogging()) {
            log.info("==> COUNT : {}", countSql);
        }
        Long total = jdbc.queryForObject(countSql, params, Long.class);
        return total != null ? total : 0;
    }

    /** 根据数据库类型生成带 LIMIT/OFFSET 的分页 SQL（MySQL / SQLServer / Oracle / PostgreSQL 自适应） */
    private String buildLimitSql(String renderedSql, int size, int currentPage) {
        String dbType = detectDbType();
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

    /** 从 JDBC URL 检测数据库类型，结果缓存 */
    private String detectDbType() {
        DataSource ds = jdbc.getJdbcTemplate().getDataSource();
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

    /** 统一日志输出 */
    private void logSql(String sql, Map<String, Object> params) {
        if (properties.isLogging()) {
            log.info("==>  SQL : {}", sql);
            log.info("==> PARAMS: {}", params);
        }
    }
}
