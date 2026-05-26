package com.undraw.util.jdbc;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.DateUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.bean.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
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

    public List<Map<String, Object>> listByKey(Model<T> model) {
        valid(model);
        List<Map<String, Object>> columns = model.getColumns();
        Map<String, Object> data = model.getData();
        List list = model.getList();
        if (StrUtils.isEmpty(list)) {
            list.add(data);
        }
        List<String> keys = StrUtils.toList(model.getKey(), ",");

        StringBuilder where = new StringBuilder();
        StringJoiner columnNames = new StringJoiner(", ");
        List<Object> args = new ArrayList<>();

        for (String key : keys) {
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("COLUMN_NAME");
                String property = StrUtils.toCamelCase(columnName);
                if (Objects.equals(key, property)) {
                    columnNames.add(columnName);
                    break;
                }
            }
        }

        where.append("WHERE ");
        where.append(String.format("(%s)", columnNames.toString()));
        where.append(" IN (");
        StringJoiner column = new StringJoiner(",");
        for (int i = 0; i < list.size(); i++) {
            for (String key : keys) {
                Object o = list.get(i);
                args.add(BeanUtils.getFieldValue(o, key));
            }
            String join = String.join(",", Collections.nCopies(keys.size(), "?"));
            column.add("(" + join + ")");
        }
        where.append(column.toString());
        where.append(")");

        String sql = String.format("SELECT * FROM %s %s", model.getTableName(), where);
        return jdbcTemplate.query(sql, new ColumnMapRowMapper(), args.toArray());
    }

    public int insert(Model model) {
        valid(model);
        List<Map<String, Object>> columns = model.getColumns();
        Map<String, Object> data = model.getData();

        StringJoiner select = new StringJoiner(", ");
        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            select.add(columnName);
        }
        String join = String.join(",", Collections.nCopies(columns.size(), "?"));
        String sql = String.format("INSERT %s (%s) VALUES (%s)", model.getTableName(), select.toString(), join);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int update = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("COLUMN_NAME");
                String property = StrUtils.toCamelCase(columnName); //属性名称
                List<String> timeList = Arrays.asList("createTime", "updateTime");
                if (timeList.contains(property)) {
                    ps.setObject(i, DateUtils.now());
                } else {
                    ps.setObject(i, data.get(property));
                }
                i++;
            }
            return ps;
        }, keyHolder);
        data.put("id", Long.valueOf(keyHolder.getKey().toString()));
        return update;
    }

    public int insertBatch(Model model) {
        valid(model);
        List<Map<String, Object>> columns = model.getColumns();
        List list = model.getList();

        StringJoiner select = new StringJoiner(", ");
        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            select.add(columnName);
        }
        String join = String.join(",", Collections.nCopies(columns.size(), "?"));
        String sql = String.format("INSERT %s (%s) VALUES (%s)", model.getTableName(), select.toString(), join);

        List<Object[]> batchArgs = new ArrayList<>();
        List<String> timeList = Arrays.asList("createTime", "updateTime");

        for (Object o : list) {
            List<Object> args = new ArrayList<>();
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("COLUMN_NAME");
                String property = StrUtils.toCamelCase(columnName); //属性名称
                if (timeList.contains(property)) {
                    args.add(DateUtils.now());
                } else {
                    args.add(BeanUtils.getFieldValue(o, property));
                }
            }
            batchArgs.add(args.toArray());
        }

        int[] n = jdbcTemplate.batchUpdate(sql, batchArgs);
        return Arrays.stream(n).sum();
    }

    public int updateBatch(Model model) {
        valid(model);
        List<Map<String, Object>> columns = model.getColumns();
        List list = model.getList();
        List<String> keys = new ArrayList<>();
        List<String> keys0 = StrUtils.toList(model.getKey(), ",");

        StringJoiner set = new StringJoiner(", ");
        List<String> fieldNames = new ArrayList<>();
        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            fieldNames.add(property);
            set.add(String.format("%s = IFNULL(?, %s)", columnName, columnName));

            if (keys0.contains(property)) {
                keys.add(property);
            }
        }

        StringJoiner where = new StringJoiner(" AND ", "WHERE ", "");
        for (String key : keys) {
            String columnName = StrUtils.toUnderScoreCase(key);
            where.add(String.format("%s = ?", columnName));
        }
        List<Object[]> batchArgs = new ArrayList<>();
        for (Object o : list) {
            List<Object> args = new ArrayList<>();
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("COLUMN_NAME");
                String property = StrUtils.toCamelCase(columnName); //属性名称
                if (Objects.equals(property, "updateTime")) {
                    args.add(DateUtils.now());
                } else if (Objects.equals(property, "createTime")) {
                    args.add(null);
                } else {
                    args.add(BeanUtils.getFieldValue(o, property));
                }
            }
            for (String key : keys) {
                Object fieldValue = BeanUtils.getFieldValue(o, key);
                args.add(fieldValue);
            }
            batchArgs.add(args.toArray());
        }

        String sql = String.format("UPDATE %s SET %s %s", model.getTableName(), set, where);
        int[] n = jdbcTemplate.batchUpdate(sql, batchArgs);
        return Arrays.stream(n).sum();
    }

    public int deleteBatch(Model model) {
        valid(model);
        List<Map<String, Object>> columns = model.getColumns();
        List list = model.getList();
        List<String> keys = new ArrayList<>();
        List<String> keys0 = StrUtils.toList(model.getKey(), ",");
        StringJoiner where = new StringJoiner(" AND ");
        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            if (keys0.contains(property)) {
                keys.add(property);
            }
        }

        for (String key : keys) {
            String columnName = StrUtils.toUnderScoreCase(key);
            where.add(String.format("%s = ?", columnName));
        }

        List<Object[]> batchArgs = new ArrayList<>();
        for (Object o : list) {
            List<Object> args = new ArrayList<>();
            for (String key : keys) {
                args.add(BeanUtils.getFieldValue(o, key));
            }
            batchArgs.add(args.toArray());
        }

        String sql = String.format("DELETE FROM %s WHERE %s", model.getTableName(), where);
        int[] n = jdbcTemplate.batchUpdate(sql, batchArgs);
        return Arrays.stream(n).sum();
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
