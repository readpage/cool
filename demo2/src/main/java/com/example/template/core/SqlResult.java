package com.example.template.core;

/** SQL 模板渲染结果。 */
public class SqlResult {

    private final String sql;

    public SqlResult(String sql) {
        this.sql = sql;
    }

    public String getSql() { return sql; }
}
