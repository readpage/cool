package com.example.template;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Query 模板引擎配置属性。
 *
 * <pre>
 * cool:
 *   sql:
 *     logging: true   # 开启 SQL 日志，默认 false
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "cool.sql")
public class QueryProperties {

    /** 是否输出 SQL 执行日志，默认 false */
    private boolean logging = false;

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }
}
