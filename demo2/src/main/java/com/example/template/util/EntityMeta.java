package com.example.template.util;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体元数据缓存 — 从 MyBatis Plus 注解读取表名、主键、字段→列名映射。
 *
 * <ul>
 *   <li>表名：{@code @TableName("xxx")}，无注解时自动驼峰转下划线</li>
 *   <li>主键：{@code @TableId} 标注的字段，同时读取 value 指定列名</li>
 *   <li>列名：{@code @TableField("col")} 显式指定，否则驼峰转下划线</li>
 *   <li>过滤：{@code @TableField(exist = false)} 不映射数据库列</li>
 * </ul>
 */
public final class EntityMeta {

    private EntityMeta() {}

    /** key = 实体类 Class */
    private static final Map<Class<?>, Meta> CACHE = new ConcurrentHashMap<>();

    // ----- public API ------------------------------------------------------

    /** 获取表名 */
    public static String getTableName(Class<?> clazz) {
        return of(clazz).tableName;
    }

    /** 获取主键 Java 字段名 */
    public static String getIdField(Class<?> clazz) {
        return of(clazz).idField;
    }

    /** 获取主键数据库列名 */
    public static String getIdColumn(Class<?> clazz) {
        return of(clazz).idColumn;
    }

    /** 字段名 → 数据库列名（驼峰转下划线 或 @TableField 指定） */
    public static String column(Class<?> clazz, String fieldName) {
        Meta m = of(clazz);
        String col = m.fieldColumnMap.get(fieldName);
        return col != null ? col : ColumnExtractor.toUnderScoreCase(fieldName);
    }

    /** 获取所有映射到数据库的 Java 字段名（排除 exist=false、static、transient） */
    public static Set<String> getFields(Class<?> clazz) {
        return of(clazz).fields;
    }

    // ----- 内部实现 --------------------------------------------------------

    /**
     * 懒加载并缓存。
     * <p>注意：元数据构建需要反射，使用 synchronized 块防并发重复构建。</p>
     */
    private static Meta of(Class<?> clazz) {
        return CACHE.computeIfAbsent(clazz, EntityMeta::build);
    }

    private static Meta build(Class<?> clazz) {
        String tableName = resolveTableName(clazz);
        String idField = null;
        String idColumn = null;
        Map<String, String> fieldColumnMap = new LinkedHashMap<>();
        Set<String> fields = new LinkedHashSet<>();

        for (Field f : getAllFields(clazz)) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;

            String fieldName = f.getName();
            TableField tf = f.getAnnotation(TableField.class);
            if (tf != null && !tf.exist()) continue;  // exist=false → 排除

            TableId tid = f.getAnnotation(TableId.class);
            if (tid != null) {
                idField = fieldName;
                idColumn = tid.value().isEmpty()
                        ? ColumnExtractor.toUnderScoreCase(fieldName)
                        : tid.value();
            }

            // 列名：@TableField value > 驼峰转下划线
            String colName = (tf != null && !tf.value().isEmpty())
                    ? tf.value()
                    : ColumnExtractor.toUnderScoreCase(fieldName);
            fieldColumnMap.put(fieldName, colName);
            fields.add(fieldName);
        }

        return new Meta(tableName, idField, idColumn, fieldColumnMap, fields);
    }

    /** 获取所有字段（包括父类），子类覆盖父类 */
    private static List<Field> getAllFields(Class<?> clazz) {
        Map<String, Field> map = new LinkedHashMap<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                map.putIfAbsent(f.getName(), f);
            }
        }
        return new ArrayList<>(map.values());
    }

    /** 解析表名：@TableName > 类名驼峰转下划线 */
    private static String resolveTableName(Class<?> clazz) {
        TableName ann = clazz.getAnnotation(TableName.class);
        if (ann != null && !ann.value().isEmpty()) {
            return ann.value();
        }
        return ColumnExtractor.toUnderScoreCase(clazz.getSimpleName());
    }

    // ----- 内部 POJO -------------------------------------------------------

    private static class Meta {
        final String tableName;
        final String idField;
        final String idColumn;
        final Map<String, String> fieldColumnMap;
        final Set<String> fields;

        Meta(String tableName, String idField, String idColumn,
             Map<String, String> fieldColumnMap, Set<String> fields) {
            this.tableName = tableName;
            this.idField = idField;
            this.idColumn = idColumn;
            this.fieldColumnMap = fieldColumnMap;
            this.fields = fields;
        }
    }
}
