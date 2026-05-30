package com.example.template.util;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 批量 SQL 构建器，支持 INSERT / DELETE / UPDATE / UPSERT。
 *
 * <ul>
 *   <li>INSERT：跳过 null 字段，依赖数据库默认值；按非空字段集分组执行</li>
 *   <li>UPDATE：不更新 null 字段；按非空字段集分组执行</li>
 *   <li>DELETE：单 key 用 IN，多 key 用行值列表</li>
 *   <li>UPSERT：INSERT ... ON DUPLICATE KEY UPDATE（MySQL）</li>
 *   <li>自动拆分超大批次（MAX_BATCH_SIZE = 500）</li>
 * </ul>
 */
final class BatchSqlBuilder {

    private BatchSqlBuilder() {}

    static final int MAX_BATCH_SIZE = 500;

    // ==================== INSERT ====================

    /**
     * 构建批量 INSERT。
     * <p>跳过 null 字段，按相同非空字段集分组，每组一个 SQL。</p>
     */
    static List<BatchSql> buildInsert(Class<?> entityClass, List<?> entities) {
        return groupAndBuild(entityClass, entities, null, false);
    }

    // ==================== DELETE ====================

    /**
     * 构建批量 DELETE（按 keyFields）。
     * <p>单 key：{@code WHERE key IN (:p0, :p1, ...)}</p>
     * <p>多 key：{@code WHERE (k1,k2) IN ((:k1_0,:k2_0), ...)}</p>
     */
    static List<BatchSql> buildDelete(Class<?> entityClass, List<?> entities, List<String> keyFields) {
        if (keyFields.size() == 1) {
            return buildDeleteBySingleKey(entityClass, entities, keyFields.get(0));
        }
        return buildDeleteByMultiKeys(entityClass, entities, keyFields);
    }

    // ==================== UPDATE ====================

    /**
     * 构建批量 UPDATE（按 keyFields 定位，只更新非 null 字段）。
     * <p>按相同非空字段集分组，每组一个 SQL。</p>
     */
    static List<BatchSql> buildUpdate(Class<?> entityClass, List<?> entities, List<String> keyFields) {
        return groupAndBuild(entityClass, entities, keyFields, true);
    }

    // ==================== UPSERT ====================

    /**
     * 构建批量 UPSERT（INSERT ... ON DUPLICATE KEY UPDATE）。
     * <p>跳过 null 字段插入，conflict 触发时更新非 null 字段。</p>
     */
    static List<BatchSql> buildUpsert(Class<?> entityClass, List<?> entities, List<String> conflictKeys) {
        String table = EntityMeta.getTableName(entityClass);
        List<BatchSql> result = new ArrayList<>();

        for (List<?> chunk : chunk(entities, MAX_BATCH_SIZE)) {
            Set<String> allFields = new LinkedHashSet<>();
            List<Map<String, Object>> allParams = new ArrayList<>();
            for (Object entity : chunk) {
                Map<String, Object> rowData = extractNonNull(entity, entityClass);
                autoFillTime(entityClass, rowData, false);  // INSERT 填充：createTime + updateTime
                allFields.addAll(rowData.keySet());
                allParams.add(rowData);
            }

            // 补齐缺失字段：同批次中某实体未提供的字段用 null 填充，确保 SQL 参数完整
            for (Map<String, Object> params : allParams) {
                for (String f : allFields) {
                    params.putIfAbsent(f, null);
                }
            }

            Set<String> colNames = new LinkedHashSet<>();
            for (String f : allFields) colNames.add(EntityMeta.column(entityClass, f));

            // INSERT columns & values
            List<String> cols = new ArrayList<>();
            List<String> vals = new ArrayList<>();
            for (String f : allFields) {
                String c = EntityMeta.column(entityClass, f);
                cols.add("`" + c + "`");
                vals.add(":" + f);
            }

            // ON DUPLICATE KEY UPDATE（COALESCE 保证 null 值不覆盖已有数据；createTime 不参与更新）
            List<String> updates = new ArrayList<>();
            for (String f : allFields) {
                if (conflictKeys.contains(f) || "createTime".equals(f)) continue;
                String c = EntityMeta.column(entityClass, f);
                updates.add("`" + c + "` = COALESCE(VALUES(`" + c + "`), `" + c + "`)");
            }

            StringBuilder sql = new StringBuilder("INSERT INTO `").append(table).append("` (");
            sql.append(String.join(", ", cols)).append(") VALUES (");
            sql.append(String.join(", ", vals)).append(")");
            if (!updates.isEmpty()) {
                sql.append(" ON DUPLICATE KEY UPDATE ").append(String.join(", ", updates));
            }

            SqlParameterSource[] srcs = allParams.stream()
                    .map(p -> new MapSqlParameterSource(p))
                    .toArray(SqlParameterSource[]::new);
            result.add(new BatchSql(sql.toString(), srcs));
        }
        return result;
    }

    // ==================== 分组构建（INSERT / UPDATE） ====================

    /**
     * @param keyFields UPDATE 时为 WHERE 条件字段，INSERT 时为 null
     * @param isUpdate  true=UPDATE, false=INSERT
     */
    private static List<BatchSql> groupAndBuild(Class<?> entityClass, List<?> entities,
                                                 List<String> keyFields, boolean isUpdate) {
        String table = EntityMeta.getTableName(entityClass);
        List<BatchSql> result = new ArrayList<>();

        for (List<?> chunk : chunk(entities, MAX_BATCH_SIZE)) {
            // 按"非空字段名集合"分组
            Map<Set<String>, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (Object entity : chunk) {
                Map<String, Object> rowData = extractNonNull(entity, entityClass);
                // 自动填充时间字段（字段为 null 时才填充，不覆盖已有值）
                autoFillTime(entityClass, rowData, isUpdate);
                // UPDATE：移除 key 字段（它们只在 WHERE 中，不在 SET 中）
                Set<String> fieldSet;
                if (isUpdate && keyFields != null) {
                    Map<String, Object> setData = new LinkedHashMap<>(rowData);
                    for (String kf : keyFields) setData.remove(kf);
                    fieldSet = setData.keySet();
                } else {
                    fieldSet = rowData.keySet();
                }
                groups.computeIfAbsent(fieldSet, k -> new ArrayList<>()).add(rowData);
            }

            for (Map.Entry<Set<String>, List<Map<String, Object>>> entry : groups.entrySet()) {
                Set<String> fieldSet = entry.getKey();
                List<Map<String, Object>> rows = entry.getValue();
                String sql = isUpdate
                        ? buildUpdateSql(table, entityClass, fieldSet, keyFields)
                        : buildInsertSql(table, entityClass, fieldSet);
                SqlParameterSource[] srcs = rows.stream()
                        .map(MapSqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                result.add(new BatchSql(sql, srcs));
            }
        }
        return result;
    }

    private static String buildInsertSql(String table, Class<?> entityClass, Set<String> fields) {
        List<String> cols = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        for (String f : fields) {
            cols.add("`" + EntityMeta.column(entityClass, f) + "`");
            vals.add(":" + f);
        }
        return "INSERT INTO `" + table + "` (" + String.join(", ", cols) + ") VALUES (" + String.join(", ", vals) + ")";
    }

    private static String buildUpdateSql(String table, Class<?> entityClass, Set<String> setFields, List<String> keyFields) {
        List<String> sets = new ArrayList<>();
        for (String f : setFields) {
            sets.add("`" + EntityMeta.column(entityClass, f) + "` = :" + f);
        }
        List<String> wheres = new ArrayList<>();
        for (String kf : keyFields) {
            wheres.add("`" + EntityMeta.column(entityClass, kf) + "` = :" + kf);
        }
        return "UPDATE `" + table + "` SET " + String.join(", ", sets) + " WHERE " + String.join(" AND ", wheres);
    }

    // ==================== DELETE ====================

    /** 单 key：WHERE key IN (:p0, :p1, ...)，每 500 个拆分 */
    private static List<BatchSql> buildDeleteBySingleKey(Class<?> entityClass, List<?> entities, String keyField) {
        String table = EntityMeta.getTableName(entityClass);
        String col = EntityMeta.column(entityClass, keyField);
        List<BatchSql> result = new ArrayList<>();

        for (List<?> chunk : chunk(entities, MAX_BATCH_SIZE)) {
            List<String> paramNames = new ArrayList<>();
            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < chunk.size(); i++) {
                String pn = "p" + i;
                paramNames.add(":" + pn);
                params.put(pn, getFieldValue(chunk.get(i), keyField));
            }
            String sql = "DELETE FROM `" + table + "` WHERE `" + col + "` IN (" + String.join(", ", paramNames) + ")";
            result.add(new BatchSql(sql, new SqlParameterSource[]{new MapSqlParameterSource(params)}));
        }
        return result;
    }

    /** 多 key：WHERE (k1,k2) IN ((:k1_0,:k2_0), (...), ...) */
    private static List<BatchSql> buildDeleteByMultiKeys(Class<?> entityClass, List<?> entities, List<String> keyFields) {
        String table = EntityMeta.getTableName(entityClass);
        List<BatchSql> result = new ArrayList<>();

        for (List<?> chunk : chunk(entities, MAX_BATCH_SIZE)) {
            List<String> colNames = new ArrayList<>();
            for (String kf : keyFields) colNames.add("`" + EntityMeta.column(entityClass, kf) + "`");

            List<String> rowTuple = new ArrayList<>();
            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < chunk.size(); i++) {
                List<String> tupleParams = new ArrayList<>();
                for (String kf : keyFields) {
                    String pn = kf + "_" + i;
                    tupleParams.add(":" + pn);
                    params.put(pn, getFieldValue(chunk.get(i), kf));
                }
                rowTuple.add("(" + String.join(", ", tupleParams) + ")");
            }
            String sql = "DELETE FROM `" + table + "` WHERE (" + String.join(", ", colNames) + ") IN (" + String.join(", ", rowTuple) + ")";
            result.add(new BatchSql(sql, new SqlParameterSource[]{new MapSqlParameterSource(params)}));
        }
        return result;
    }

    // ==================== 工具方法 ====================

    /** 提取实体中所有非 null 字段 → 值映射 */
    private static Map<String, Object> extractNonNull(Object entity, Class<?> entityClass) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String field : EntityMeta.getFields(entityClass)) {
            Object val = getFieldValue(entity, field);
            if (val != null) {
                map.put(field, val);
            }
        }
        return map;
    }

    /**
     * 自动填充时间字段（仿 MyBatis-Plus MetaObjectHandler）。
     * <ul>
     *   <li>INSERT：createTime 和 updateTime 为 null 时自动填充当前时间</li>
     *   <li>UPDATE：仅 updateTime 为 null 时自动填充当前时间</li>
     * </ul>
     * <p>若实体中已有值则不做覆盖。</p>
     */
    private static void autoFillTime(Class<?> entityClass, Map<String, Object> rowData, boolean isUpdate) {
        Set<String> fields = EntityMeta.getFields(entityClass);
        LocalDateTime now = LocalDateTime.now();
        if (fields.contains("createTime") && !isUpdate) {
            rowData.putIfAbsent("createTime", now);
        }
        if (fields.contains("updateTime")) {
            rowData.putIfAbsent("updateTime", now);
        }
    }

    /** 反射获取字段值（先用 getter，再 fallback 到直接访问） */
    private static Object getFieldValue(Object entity, String fieldName) {
        try {
            String cap = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            // 先尝试 getXxx()
            try {
                return entity.getClass().getMethod("get" + cap).invoke(entity);
            } catch (NoSuchMethodException e) {
                // 再尝试 isXxx()（Boolean 类型）
                try {
                    return entity.getClass().getMethod("is" + cap).invoke(entity);
                } catch (NoSuchMethodException e2) {
                    // fallback：直接访问字段
                    Field f = findField(entity.getClass(), fieldName);
                    if (f != null) {
                        f.setAccessible(true);
                        return f.get(entity);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("无法读取字段值: " + entity.getClass().getName() + "." + fieldName, e);
        }
        return null;
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    /** 按 MAX_BATCH_SIZE 拆分 List */
    static <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }

    // ==================== record ====================

    record BatchSql(String sql, SqlParameterSource[] params) {}
}
