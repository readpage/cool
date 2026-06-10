package com.example.template.datasource;

import com.example.domain.entity.Datasource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态 JDBC 工厂 — 按数据源实体创建/缓存 NamedParameterJdbcTemplate。
 * <p>每个数据源一个 HikariCP 连接池，读写完全隔离。
 * <p>NULL 数据源 ID 或实体返回主数据源（Spring 注入的 primaryJdbc）。
 */
@Slf4j
@Component
public class DynamicJdbcFactory {

    /** 数据源 ID → NamedParameterJdbcTemplate（含独立 HikariCP 连接池） */
    private final Map<Long, NamedParameterJdbcTemplate> cache = new ConcurrentHashMap<>();

    /** 主数据源（datasourceId 为 null 时使用） */
    private final NamedParameterJdbcTemplate primaryJdbc;

    public DynamicJdbcFactory(NamedParameterJdbcTemplate primaryJdbc) {
        this.primaryJdbc = primaryJdbc;
    }

    /**
     * 根据数据源 ID 获取 JdbcTemplate。
     * @param datasourceId 数据源 ID，null 返回主数据源
     */
    public NamedParameterJdbcTemplate getJdbcTemplate(Long datasourceId) {
        if (datasourceId == null) {
            return primaryJdbc;
        }
        return cache.get(datasourceId);
    }

    /**
     * 根据完整数据源实体获取或创建 JdbcTemplate（推荐方式）。
     * 若缓存中已存在则直接返回，否则创建新的 HikariCP 连接池。
     */
    public NamedParameterJdbcTemplate getOrCreate(Datasource ds) {
        if (ds == null || ds.getId() == null) {
            return primaryJdbc;
        }
        return cache.computeIfAbsent(ds.getId(), id -> createJdbcTemplate(ds));
    }

    /**
     * 刷新数据源连接池 — 原子 swap，无空窗期。
     * <ol>
     *   <li>用最新配置创建新连接池</li>
     *   <li>原子替换缓存中的旧池（put 返回旧值）</li>
     *   <li>软关闭旧池（在途查询不受影响）</li>
     * </ol>
     */
    public NamedParameterJdbcTemplate refresh(Datasource ds) {
        if (ds == null || ds.getId() == null) return primaryJdbc;

        // 1. 先创建新池
        NamedParameterJdbcTemplate newOne = createJdbcTemplate(ds);

        // 2. 原子替换（put 返回旧值）
        NamedParameterJdbcTemplate old = cache.put(ds.getId(), newOne);

        // 3. 关闭旧池（新池已就位，不影响查询）
        if (old != null && old != newOne) {
            closeQuietly(old);
        }

        return newOne;
    }

    /**
     * 关闭并移除数据源连接池（数据源被删除时调用）。
     */
    public void evict(Long datasourceId) {
        if (datasourceId == null) return;
        NamedParameterJdbcTemplate old = cache.remove(datasourceId);
        if (old != null) {
            closeQuietly(old);
        }
    }

    // ====== 工具方法 ======

    /**
     * 测试连接（不复用连接池，独立建连后即关闭）。
     * @return null 表示连接成功；非 null 为人性化错误描述
     */
    public static String testConnect(Datasource ds) {
        String url = buildJdbcUrl(ds);
        Properties props = new Properties();
        props.setProperty("user", ds.getUsername());
        props.setProperty("password", ds.getPassword());
        props.setProperty("connectTimeout", "5000");
        props.setProperty("socketTimeout", "5000");
        try (Connection conn = DriverManager.getConnection(url, props)) {
            if (conn.isValid(3)) {
                return null; // 成功
            }
            return "连接校验失败，请检查数据库是否可用";
        } catch (SQLException e) {
            log.warn("测试连接失败: name={} url={}", ds.getName(), url, e);
            return parseConnectionError(e);
        }
    }

    /**
     * 将 SQLException 翻译为人性化中文错误提示。
     */
    private static String parseConnectionError(SQLException e) {
        if (e == null) return "未知连接错误";
        String msg = e.getMessage();
        String causeMsg = e.getCause() != null ? e.getCause().getMessage() : "";
        String combined = (msg != null ? msg.toLowerCase() : "")
            + " " + (causeMsg != null ? causeMsg.toLowerCase() : "");

        if (combined.contains("access denied for user")) {
            return "用户名或密码错误";
        }
        if (combined.contains("unknown database")) {
            return "数据库名不存在，请检查库名填写是否正确";
        }
        if (combined.contains("communications link failure")
            || combined.contains("connection refused")
            || combined.contains("connect timed out")) {
            return "无法连接到数据库服务器，请检查主机地址和端口号";
        }
        if (combined.contains("public key retrieval")) {
            return "MySQL 公钥获取被拒绝，请在连接参数中添加 allowPublicKeyRetrieval=true";
        }
        if (combined.contains("unknownhostexception")
            || combined.contains("no such host")
            || combined.contains("nodename nor servname")
            || combined.contains("unknown host")) {
            return "无法解析主机地址，请检查主机名是否正确";
        }
        if (combined.contains("timeout") || combined.contains("timed out")) {
            return "连接超时，请检查网络或数据库防火墙设置";
        }
        // 兜底：截取原始错误信息，避免返回过长堆栈
        String shortMsg = msg != null ? msg : "";
        int maxLen = 200;
        if (shortMsg.length() > maxLen) {
            shortMsg = shortMsg.substring(0, maxLen) + "...";
        }
        return "连接失败: " + shortMsg;
    }

    /**
     * 根据数据源实体构建 JDBC URL。
     */
    public static String buildJdbcUrl(Datasource ds) {
        String dbType = ds.getDbType() != null ? ds.getDbType().toUpperCase() : "MYSQL";
        String baseUrl = switch (dbType) {
            case "POSTGRESQL" -> String.format(
                "jdbc:postgresql://%s:%d/%s", ds.getHost(), ds.getPort(), ds.getDbName());
            default -> String.format(
                "jdbc:mysql://%s:%d/%s", ds.getHost(), ds.getPort(), ds.getDbName());
        };
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (ds.getParams() != null && !ds.getParams().isBlank()) {
            urlBuilder.append(baseUrl.contains("?") ? "&" : "?").append(ds.getParams());
        }
        // MySQL 8.0+ 自动追加必要参数
        if (!"POSTGRESQL".equals(dbType)) {
            String currentParams = urlBuilder.toString().toLowerCase();
            if (!currentParams.contains("allowpublickeyretrieval")) {
                urlBuilder.append(currentParams.contains("?") ? "&" : "?")
                    .append("allowPublicKeyRetrieval=true");
            }
            if (!currentParams.contains("usessl")) {
                urlBuilder.append("&useSSL=false");
            }
        }
        return urlBuilder.toString();
    }

    // ====== 内部方法 ======

    /** 创建 HikariCP 连接池 → NamedParameterJdbcTemplate */
    private NamedParameterJdbcTemplate createJdbcTemplate(Datasource ds) {
        log.info("创建数据源连接池: id={} name={} dbType={} host={}:{}/{}",
                ds.getId(), ds.getName(), ds.getDbType(), ds.getHost(), ds.getPort(), ds.getDbName());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(buildJdbcUrl(ds));
        config.setUsername(ds.getUsername());
        config.setPassword(ds.getPassword());
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setPoolName("ReportDS-" + ds.getId());

        HikariDataSource dataSource = new HikariDataSource(config);
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /** 软关闭旧连接池 */
    private void closeQuietly(NamedParameterJdbcTemplate jdbc) {
        try {
            DataSource ds = jdbc.getJdbcTemplate().getDataSource();
            if (ds instanceof HikariDataSource hds) {
                log.info("关闭旧数据源连接池: {}", hds.getPoolName());
                hds.close();
            }
        } catch (Exception e) {
            log.warn("关闭连接池异常", e);
        }
    }

    @PreDestroy
    public void destroy() {
        cache.forEach((id, jdbc) -> closeQuietly(jdbc));
        cache.clear();
        log.info("已关闭所有动态数据源连接池");
    }
}
