package com.undraw.util.jdbc;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.StrUtils;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JdbcUtil {

    @Resource
    private JdbcTemplate jdbcTemplate;


    public List<Map<String, Object>> columnList(String tableName) {
        String sql = "SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
        return jdbcTemplate.query(sql, new ColumnMapRowMapper(), tableName);
    }

    public Map<String, Object> table(String tableName) {
        String sql = "SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = ? LIMIT 1";
        return jdbcTemplate.queryForObject(sql, new ColumnMapRowMapper(), tableName);
    }

    private void valid(Model model) {
        String tableName = model.getTableName();
        Map<String, Object> table = table(tableName);
        if (table == null) {
            throw new CustomerException(tableName + "表不存在");
        }
        model.setTable(table);
        model.setColumns(columnList(tableName));
    }

    public <T> List<T> list(Model<T> model) {
        valid(model);
        Class<T> type = model.getType();
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();
        Map<String, Object> data = model.getData();

        StringJoiner select = new StringJoiner(", ");
        String show = (String) data.get("show");
        String visible = (String) data.get("visible");
        List<String> fieldNames = new ArrayList<>();
        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            fieldNames.add(property);
            if (StrUtils.isNotEmpty(show)) {
                if (show.contains(property)) {
                    select.add(columnName);
                }
            } else if (StrUtils.isNotEmpty(visible)) {
                if (!visible.contains(property)) {
                    select.add(columnName);
                }
            } else {
                select.add(columnName);
            }
        }

        List<Object> args = new ArrayList<>();
        String where = where0(fieldNames, data, args);
        String sql = String.format("SELECT %s FROM %s %s", select.toString(), model.getTableName(), where);
        List list = new ArrayList<>();
        if (type != null) {
            list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(type), args.toArray());
        } else {
            list = jdbcTemplate.query(sql, new ColumnMapRowMapper(), args.toArray());
        }
        return list;
    }

    public List<Map<String, Object>> listMaps(Model model) {
        return (List<Map<String, Object>>)this.list(model);
    }

    public <T> List<T> listByKey(Model<T> model) {
        valid(model);
        Class<T> type = model.getType();
        Map<String, Object> table = model.getTable();
        List<Map<String, Object>> columns = model.getColumns();
        Map<String, Object> data = model.getData();
        List list = model.getList();
        if (StrUtils.isEmpty(list)) {
            list.add(data);
        }
        List<String> keys = StrUtils.toList(model.getKey(), ",");

        StringBuilder where = new StringBuilder();
        StringJoiner select = new StringJoiner(", ");
        StringJoiner columnNames = new StringJoiner(", ");

        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            select.add(columnName);
            if (keys.contains(property)) {
                columnNames.add(columnName);
            }
        }
        where.append(String.format("(%s)", columnNames.toString()));
        where.append(" IN (");
        for (int i = 0; i < list.size(); i++) {
            String join = String.join(",", Collections.nCopies(list.size(), "?"));
        }
        where.append(")");
        return new ArrayList<>();
    }

    public int insert(Model model) {
        return 0;
    }

    private String where0(List<String> fieldNames, Map<String, Object> data, List<Object> args) {
        String sql = "";
        StringJoiner where = new StringJoiner(" AND ");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            for (String fieldName : fieldNames) {
                String columnName = StrUtils.toUnderScoreCase(fieldName);
                if (key.endsWith("Like") && Objects.equals(fieldName, key.substring(0, key.length() - 4))) {

                } else if (Objects.equals(fieldName, key)) {
                    if (StrUtils.isNotEmpty(value)) {
                        where.add(columnName + " = ?");
                        args.add(value);
                    }
                    break;
                }
            }
        }
        if (StrUtils.isNotEmpty(where.toString())) {
            sql = "WHERE " + where.toString();
        }
        return sql;
    }
}
