package com.example.template.util;

import com.example.template.QueryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
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
 *
 * <p>批量操作自动从实体类注解读取表名、主键和字段映射（@TableName / @TableId / @TableField）。</p>
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

    // ==================== 查询 ====================

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

    /** 从 SQL 截取 COUNT：移除 ORDER BY，SELECT ... FROM → SELECT COUNT(*) FROM */
    private String buildCountSql(String renderedSql) {
        String sql = renderedSql.replaceAll("(?i)\\s+ORDER\\s+BY\\s+.*$", "");
        return sql.replaceAll("(?i)^\\s*SELECT\\s+.+?\\s+FROM\\s+", "SELECT COUNT(*) FROM ");
    }
}
