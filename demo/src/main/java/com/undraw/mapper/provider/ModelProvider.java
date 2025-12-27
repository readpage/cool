package com.undraw.mapper.provider;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DateUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.bean.BeanUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ModelProvider {

    public String insert(Model model) {
        Map<String, Object> data = model.getData();
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();
        if (data == null) {
            return "";
        }

//        批量插入
//        SQL sql = new SQL();
//        sql.INSERT_INTO("user");
//        sql.INTO_COLUMNS("id", "name", "email");
//        sql.INTO_VALUES(user.getId(), user.getName(), user.getEmail());

        return new SQL() {
            {
                String tableName = String.valueOf(table.get("TABLE_NAME"));
                INSERT_INTO(tableName);
                for (Map<String, Object> column : columns) {
                    String columnName = (String) column.get("COLUMN_NAME");
                    String property = StrUtils.toCamelCase(columnName); //属性名称
                    List<String> timeList = Arrays.asList("createTime", "updateTime");
                    if (timeList.contains(property)) {
                        VALUES(columnName, String.format("'%s'", DateUtils.now()));
                        continue;
                    }

                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (Objects.equals(property, key)) {
                            if (StrUtils.isNotEmpty(value)) {
                                VALUES(columnName, String.format("#{data.%s}", key));
                            }
                            break;
                        }
                    }
                }
            }
        }.toString();
    }

    public String insertBatch(Model model) {
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();
        List list = model.getList();

        if (list == null || list.size() == 0) {
            return "";
        }
        Object o = list.get(0);
        List<String> timeList = Arrays.asList("createTime", "updateTime");
        Map<String, Object> map = new HashMap<>();

        if (o instanceof Map) {
            map = (Map) o;
        } else {
            map = BeanUtils.getEntry(o);
        }

        StringBuilder sql = new StringBuilder();
        String tableName = String.valueOf(table.get("TABLE_NAME"));
        sql.append("INSERT INTO " + tableName);
        List<String> columnNames = new ArrayList<>();
        sql.append("(");
        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (Objects.equals(property, key)) {
                    if (StrUtils.isNotEmpty(value) || timeList.contains(property)) {
                        columnNames.add(columnName);
                        sql.append(columnName + ",");
                    }
                }
            }
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES ");

        for (int i = 0; i < list.size(); i++) {
            sql.append("(");
            for (String columnName : columnNames) {
                String property = StrUtils.toCamelCase(columnName);
                if (timeList.contains(property)) {
                    sql.append(String.format("'%s',", DateUtils.now()));
                } else {
                    sql.append(String.format("#{list[%d].%s},", i, property));
                }
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append("),");
        }
        sql.deleteCharAt(sql.length() - 1);
        return sql.toString();
    }

    public String updateBatch(Model model) {
        Map<String, Object> data = model.getData();
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();
        if (data == null) {
            return "";
        }

        List<String> keys = getKeys(model);


        StringBuilder sql = new StringBuilder();
        String tableName = String.valueOf(table.get("TABLE_NAME"));


        List<Map> list = model.getList();
        if (!(list != null && list.size() > 0)) {
            return "";
        }
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            sql.append("UPDATE " + tableName + " SET ");

            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("COLUMN_NAME");
                String property = StrUtils.toCamelCase(columnName); //属性名称

                if (Objects.equals(property, "updateTime")) {
                    sql.append(String.format(columnName + " = '%s',", DateUtils.now()));
                } else {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String k1 = entry.getKey();
                        Object v1 = entry.getValue();
                        if (Objects.equals(property, k1)) {
                            if (StrUtils.isNotEmpty(v1)) {
                                sql.append(String.format(columnName + " = #{list[%d].%s},", i, k1));
                            }
                            break;
                        }
                    }
                }
            }
            sql.deleteCharAt(sql.length() - 1);

            StringJoiner where = new StringJoiner(" AND ");
            for (String key2 : keys) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String k1 = entry.getKey();
                    Object v1 = entry.getValue();
                    if (Objects.equals(key2, k1)) {
                        if (StrUtils.isNotEmpty(v1)) {
                            where.add(String.format(StrUtils.toUnderScoreCase(k1) + " = #{list[%d].%s}", i, k1));
                        }
                        break;
                    }
                }
            }
            sql.append(" WHERE " + where.toString() + ";\n");
        }


        return sql.toString();
    }

    private static List<String> getKeys(Model model) {
        List<String> keys = new ArrayList<>();
        for (Map<String, Object> column : model.getColumns()) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName); //属性名称
            List<String> keys1 = StrUtils.toList(model.getKey(), ",");
            if (StrUtils.isEmpty(keys1)) {
                if (Objects.equals(String.valueOf(column.get("COLUMN_KEY")), "PRI")) {
                    keys.add(property);
                    break;
                }
            }
            if (keys1.contains(property)) {
                keys.add(property);
            }
        }
        return keys;
    }

    public String delete(Model model) {
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();

        if (model.getKey() == null) {
            return "DELETE FROM " + table.get("TABLE_NAME");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(String.format("DELETE FROM %s WHERE ", table.get("TABLE_NAME")));

        List list = model.getList();
        List<String> keys = getKeys(model);
        String key = StrUtils.toUnderScoreCase(keys.get(0));

        StringJoiner values = new StringJoiner(",");
        for (int i = 0; i < list.size(); i++) {
            values.add(String.format("#{model.list[%d]}", i));
        }
        sql.append(String.format("%s IN (%s)", key, values.toString()));
        return sql.toString();
    }

    public String deleteBatch(Model model) {
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();

        StringBuilder sql = new StringBuilder();

        sql.append(String.format("DELETE FROM %s WHERE ", table.get("TABLE_NAME")));

        List<Map> list = model.getList();
        List<String> keys = getKeys(model);

        StringJoiner columnNames = new StringJoiner(",");
        for (String key : keys) {
            columnNames.add(StrUtils.toUnderScoreCase(key));
        }

        StringJoiner values = new StringJoiner(",");
        for (int i = 0; i < list.size(); i++) {
            StringJoiner param = new StringJoiner(",");
            for (String key : keys) {
                param.add(String.format("#{list[%d].%s}", i, key));
            }
            values.add(String.format("CONCAT(%s)", param));
        }
        sql.append(String.format("CONCAT(%s) IN (%s)", columnNames, values));

        return sql.toString();
    }

    public String list(Model model) {
        Map<String, Object> data = model.getData();
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();

        StringBuilder sql = new StringBuilder();

        StringJoiner select = new StringJoiner(", ");
        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            String col = columnName;
            if (!Objects.equals(columnName, property)) {
                col = columnName + " " + property;
            }

            String show = (String) data.get("show");
            String visible = (String) data.get("visible");
            if (StrUtils.isNotEmpty(show)) {
                if (show.contains(property)) {
                    select.add(col);
                }
            } else if (StrUtils.isNotEmpty(visible)) {
                if (!visible.contains(property)) {
                    select.add(col);
                }
            } else {
                select.add(col);
            }

        }
        sql.append(String.format("SELECT %s FROM %s ", select.toString(), table.get("TABLE_NAME") ));

        List<String> list = columns.stream().map(column -> StrUtils.toCamelCase((String) column.get("COLUMN_NAME"))).collect(Collectors.toList());
        sql.append(where(new Query(list, data)));
        return sql.toString();
    }

    public String getOne(Model model) {
        return list(model) + " LIMIT 1";
    }

    public String listByKey(Model model) {
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();


        String sql = new SQL() {
            {
                StringJoiner select = new StringJoiner(", ");
                for (Map<String, Object> column : columns) {
                    String columnName = (String) column.get("COLUMN_NAME");
                    String property = StrUtils.toCamelCase(columnName);
                    String col = columnName;
                    if (!Objects.equals(columnName, property)) {
                        col = columnName + " " + property;
                    }
                    select.add(col);
                }

                SELECT(select.toString());
                String tableName = String.valueOf(table.get("TABLE_NAME"));
                FROM(tableName);

                String key = getKeys(model).get(0);
                StringBuilder where = new StringBuilder();
                StringBuilder columnNames = new StringBuilder();
                for (Map<String, Object> column : columns) {
                    String columnName = (String) column.get("COLUMN_NAME");
                    String property = StrUtils.toCamelCase(columnName);
                    if (key.equals(property)) {
                        columnNames.append(columnName).append(",");
                    }
                }
                columnNames.deleteCharAt(columnNames.lastIndexOf(","));
                where.append(String.format("(%s)", columnNames.toString()));
                where.append(" IN (");

                List list = model.getList();
                for (int i = 0; i < list.size(); i++) {
                    where.append("(");
                    where.append(String.format("#{model.list[%d]},", i));
                    where.delete(where.length() - 1, where.length());
                    where.append("),");
                }
                where.delete(where.length() - 1, where.length());
                where.append(")");
                WHERE(where.toString());
            }
        }.toString();
        return sql;
    }

    public String listByKeys(Model model) {
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();


        String sql = new SQL() {
            {
                StringJoiner select = new StringJoiner(", ");
                for (Map<String, Object> column : columns) {
                    String columnName = (String) column.get("COLUMN_NAME");
                    String property = StrUtils.toCamelCase(columnName);
                    String col = columnName;
                    if (!Objects.equals(columnName, property)) {
                        col = columnName + " " + property;
                    }
                    select.add(col);
                }

                SELECT(select.toString());
                String tableName = String.valueOf(table.get("TABLE_NAME"));
                FROM(tableName);

                List<String> keys = getKeys(model);
                StringBuilder where = new StringBuilder();
                StringBuilder columnNames = new StringBuilder();
                for (Map<String, Object> column : columns) {
                    String columnName = (String) column.get("COLUMN_NAME");
                    String property = StrUtils.toCamelCase(columnName);
                    if (keys.contains(property)) {
                        columnNames.append(columnName).append(",");
                    }
                }
                columnNames.deleteCharAt(columnNames.lastIndexOf(","));
                where.append(String.format("(%s)", columnNames.toString()));
                where.append(" IN (");

                List list = model.getList();
                for (int i = 0; i < list.size(); i++) {
                    where.append("(");
                    for (String key1 : keys) {
                        where.append(String.format("#{list[%d].%s},", i, key1));
                    }
                    where.delete(where.length() - 1, where.length());
                    where.append("),");
                }
                where.delete(where.length() - 1, where.length());
                where.append(")");
                WHERE(where.toString());
            }
        }.toString();
        return sql;
    }

    public String where(Query query) {
        List<String> fieldNames = query.getFieldNames();
        Map<String, Object> data = query.getData();

        StringBuilder where = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            for (String fieldName : fieldNames) {
                String columnName = StrUtils.toUnderScoreCase(fieldName);
                if (key.endsWith("Like") && Objects.equals(fieldName, key.substring(0, key.length() - 4))) {
                    if (StrUtils.isNotEmpty(value)) {
                        where.append(String.format(columnName + " LIKE CONCAT('%%', #{data.%s}, '%%')", key));
                        where.append(" AND ");
                    }
                    break;
                } else if (key.endsWith("Range") && Objects.equals(fieldName, key.substring(0, key.length() - 5))) {
                    if (StrUtils.isNotEmpty(value)) {
                        entry.setValue(StrUtils.toList(String.valueOf(value), ","));
                        where.append(String.format(columnName + " BETWEEN #{data.%s[0]} AND #{data.%s[1]}", key, key));
                        where.append(" AND ");
                    }
                    break;
                } else if (key.endsWith("In") && Objects.equals(fieldName, key.substring(0, key.length() - 2))) {
                    if (StrUtils.isNotEmpty(value)) {
                        List<String> list = StrUtils.toList(String.valueOf(value), ",");
                        entry.setValue(list);
                        StringBuilder sb = new StringBuilder();
                        sb.append(columnName + " IN (");
                        for (int i = 0; i < list.size(); i++) {
                            sb.append(String.format("#{data.%s[%d]},", key, i));
                        }
                        sb.delete(sb.length() - 1, sb.length());
                        sb.append(")");
                        where.append(sb.toString());
                        where.append(" AND ");
                    }
                    break;
                } else if (Objects.equals(fieldName, key)) {
                    if (StrUtils.isNotEmpty(value)) {
                        where.append(String.format(columnName + " = #{data.%s}", key));
                        where.append(" AND ");
                    }
                    break;
                }
            }

            if ("last".equals(key)) {
                where.append("(" + value.toString() + ")");
                where.append(" AND ");
            }
        }


        // keyword -->
        List<String> keywords = StrUtils.toList((String) data.get("keywords"), ",");
        String keyword = (String) data.get("keyword");
        if (StrUtils.isNotEmpty(keyword) && StrUtils.isNotEmpty(keyword)) {
            StringBuilder or = new StringBuilder();
            for (String fieldName : keywords) {
                if (fieldNames.contains(fieldName)) {
                    String columnName = StrUtils.toUnderScoreCase(fieldName);
                    or.append(columnName + " LIKE CONCAT('%%', #{data.keyword}, '%%')");
                    or.append(" OR ");
                }
            }
            if (or.toString().endsWith(" OR ")) {
                or.delete(or.length() - 4, or.length());
            }
            if (StrUtils.isNotEmpty(or.toString())) {
                where.append("( " + or.toString() + " )");
            }
        }
        // <-

        if (where.toString().endsWith(" AND ")) {
            where.delete(where.length() - 5, where.length());
        }

        StringBuilder sql = new StringBuilder();
        if (StrUtils.isNotEmpty(where.toString())) {
            sql.append(" WHERE " + where.toString());
            data.put("where", where.toString().replace("data.", ""));
        } else {
            data.put("where"," WHERE 1 = 1 ");
        }

        // sort -->
        Map<String, String> map = null;
        try {
            Object o = data.get("sort");
            if (Objects.equals(o, "{}")) {
                throw new CustomerException();
            }
            map = ConvertUtils.cloneDeep(o, Map.class);
        } catch (Exception e) {
            data.remove("sort");
        }

        StringJoiner sort = new StringJoiner(",");
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                for (String fieldName : fieldNames) {
                    String columnName = StrUtils.toUnderScoreCase(fieldName);
                    if (Objects.equals(key, fieldName)) {
                        if ("asc".equals(value)) {
                            sort.add(String.format(" %s ASC", columnName));
                        } else if ("desc".equals(value)) {
                            sort.add(String.format(" %s DESC", columnName));
                        }
                    }
                }
            }

            if (StrUtils.isNotEmpty(sort.toString())) {
                String format = String.format(" ORDER BY %s, id ASC", sort.toString());
                sql.append(format);
                data.put("sort", format);
            }
        }
        // <-
        return sql.toString();
    }

}
