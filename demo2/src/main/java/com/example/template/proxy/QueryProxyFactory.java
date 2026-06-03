package com.example.template.proxy;

import com.example.template.QueryProperties;
import com.example.template.annotation.Modify;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.core.SqlResult;
import com.example.template.core.SqlTemplateEngine;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;
import com.example.template.util.SqlParamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDK 动态代理工厂 —— 拦截 @Query / @Modify 注解的方法，编译模板并执行 SQL。
 */
public class QueryProxyFactory {

    private static final Logger log = LoggerFactory.getLogger(QueryProxyFactory.class);

    private final SqlTemplateEngine engine;
    private final NamedParameterJdbcTemplate jdbc;
    private final QueryProperties properties;

    /** 数据库类型缓存：DataSource → mysql|sqlserver|oracle|postgresql */
    private static final Map<DataSource, String> DB_TYPE_CACHE = new ConcurrentHashMap<>();

    public QueryProxyFactory(NamedParameterJdbcTemplate jdbc, QueryProperties properties) {
        this.engine = new SqlTemplateEngine();
        this.jdbc = jdbc;
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> daoInterface, NamedParameterJdbcTemplate jdbc, QueryProperties properties) {
        return (T) new QueryProxyFactory(jdbc, properties).newProxy(daoInterface);
    }

    @SuppressWarnings("unchecked")
    public <T> T newProxy(Class<T> daoInterface) {
        return (T) Proxy.newProxyInstance(daoInterface.getClassLoader(),
                new Class<?>[]{daoInterface}, new QueryInvocationHandler());
    }

    private class QueryInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Modify modify = method.getAnnotation(Modify.class);
            if (modify != null) return executeUpdate(modify.value(), method, args);
            Query query = method.getAnnotation(Query.class);
            if (query == null) throw new UnsupportedOperationException(
                    "方法 " + method.getName() + " 缺少 @Query 或 @Modify 注解");
            return executeQuery(query.value(), method, args);
        }

        private Object executeUpdate(String template, Method method, Object[] args) {
            Map<String, Object> params = resolveParams(method, args, template);
            SqlResult result = engine.render(template, params);
            log(result.getSql(), params);
            return jdbc.update(result.getSql(), params);
        }

        private Object executeQuery(String template, Method method, Object[] args) {
            Map<String, Object> params = resolveParams(method, args, template);
            SqlResult result = engine.render(template, params);
            log(result.getSql(), params);
            Class<?> returnType = method.getReturnType();

            // 分页查询：返回 PageResult
            if (PageResult.class.isAssignableFrom(returnType)) {
                return executePageQuery(result.getSql(), params, method, args);
            }

            if (List.class.isAssignableFrom(returnType)) {
                // 自动分页：FilterParam.paginate=true 时先 COUNT 再 LIMIT，total 写回 FilterParam
                FilterParam fp = findFilterParam(args);
                if (fp != null && fp.isPaginate()) {
                    return executeImplicitPageQuery(result.getSql(), params, method, fp);
                }
                Class<?> elementType = resolveElementType(method.getGenericReturnType());
                if (Map.class.isAssignableFrom(elementType))
                    return jdbc.queryForList(result.getSql(), params);
                return jdbc.query(result.getSql(), params,
                        new BeanPropertyRowMapper<>(elementType));
            }
            if (Map.class.isAssignableFrom(returnType)) {
                List<Map<String, Object>> list = jdbc.queryForList(result.getSql(), params);
                return list.isEmpty() ? null : list.get(0);
            }
            List<?> list = jdbc.query(result.getSql(), params,
                    new BeanPropertyRowMapper<>(returnType));
            return list.isEmpty() ? null : list.get(0);
        }

        /** 隐式分页：当 FilterParam.paginate=true 时，先 COUNT 再 LIMIT，total 写回 FilterParam */
        private <T> List<T> executeImplicitPageQuery(String renderedSql,
                                                      Map<String, Object> params,
                                                      Method method,
                                                      FilterParam fp) {
            int current = fp.getCurrent() > 0 ? fp.getCurrent() : 1;
            int size = fp.getSize() > 0 ? fp.getSize() : 10;

            // 1. COUNT
            String countSql = buildCountSql(renderedSql);
            log(countSql, params);
            Long total = jdbc.queryForObject(countSql, params, Long.class);
            fp.setTotal(total != null ? total : 0);

            // 2. 分页
            String limitSql = buildPageSql(renderedSql, size, current);
            Class<?> elementType = resolveElementType(method.getGenericReturnType());
            @SuppressWarnings("unchecked")
            List<T> records = (List<T>) jdbc.query(limitSql, params,
                    new BeanPropertyRowMapper<>(elementType));
            return records;
        }

        /** 分页查询：先 COUNT 再 LIMIT */
        private <T> PageResult<T> executePageQuery(String renderedSql,
                                                    Map<String, Object> params,
                                                    Method method,
                                                    Object[] args) {
            FilterParam fp = findFilterParam(args);
            int current = fp != null && fp.getCurrent() > 0 ? fp.getCurrent() : 1;
            int size    = fp != null && fp.getSize()    > 0 ? fp.getSize()    : 10;

            // 1. COUNT
            String countSql = buildCountSql(renderedSql);
            log(countSql, params);
            Long total = jdbc.queryForObject(countSql, params, Long.class);

            // 2. 分页
            String limitSql = buildPageSql(renderedSql, size, current);
            Class<?> elementType = resolveElementType(method.getGenericReturnType());
            @SuppressWarnings("unchecked")
            List<T> records = (List<T>) jdbc.query(limitSql, params,
                    new BeanPropertyRowMapper<>(elementType));

            return new PageResult<>(total != null ? total : 0, current, size, records);
        }

        /** 从 SQL 截取 COUNT：移除 ORDER BY，CTE 查询保留 WITH 在外层、仅包裹最终 SELECT */
        private String buildCountSql(String renderedSql) {
            String sql = renderedSql.replaceAll("(?i)\\s+ORDER\\s+BY\\s+.*$", "");
            if (sql.trim().toUpperCase().startsWith("WITH ")) {
                // 找到最后一个 ) ... SELECT 的分界点 —— ) 结束 CTE，SELECT 开始最终查询
                Pattern p = Pattern.compile("\\)\\s*SELECT\\s", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher m = p.matcher(sql);
                int splitPos = -1;
                while (m.find()) {
                    splitPos = m.start() + 1; // 指向 ) 之后
                }
                if (splitPos > 0) {
                    String ctePart = sql.substring(0, splitPos);
                    String finalSelect = sql.substring(splitPos);
                    return ctePart + "SELECT COUNT(*) FROM (" + finalSelect + ") _count_sub";
                }
                // 兜底：直接包裹（不应走到这里）
                return "SELECT COUNT(*) FROM (" + sql + ") _count_sub";
            }
            return sql.replaceAll("(?i)^\\s*SELECT\\s+.+?\\s+FROM\\s+", "SELECT COUNT(*) FROM ");
        }

        /** 检测当前数据源的数据库类型（从 JDBC URL 自动识别，结果缓存） */
        private String detectDbType() {
            DataSource ds = jdbc.getJdbcTemplate().getDataSource();
            if (ds == null) return "mysql";
            return DB_TYPE_CACHE.computeIfAbsent(ds, key -> {
                try (Connection conn = key.getConnection()) {
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

        /** 根据数据库类型生成分页 SQL */
        private String buildPageSql(String renderedSql, int size, int currentPage) {
            String dbType = detectDbType();
            int offset = (currentPage - 1) * size;
            if ("sqlserver".equals(dbType)) {
                // SQL Server: OFFSET/FETCH 要求 ORDER BY；若无则兜底
                if (!renderedSql.toUpperCase().contains("ORDER BY")) {
                    renderedSql += " ORDER BY (SELECT 0)";
                }
                return renderedSql + " OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY";
            }
            if ("oracle".equals(dbType)) {
                return "SELECT * FROM (SELECT _ora_.*, ROWNUM _rn_ FROM (" + renderedSql
                        + ") _ora_ WHERE ROWNUM <= " + (offset + size) + ") WHERE _rn_ > " + offset;
            }
            // MySQL / PostgreSQL 等
            return renderedSql + " LIMIT " + size + " OFFSET " + offset;
        }

        /** 从参数中提取 FilterParam */
        private FilterParam findFilterParam(Object[] args) {
            if (args == null) return null;
            for (Object arg : args) {
                if (arg instanceof FilterParam fp) return fp;
            }
            return null;
        }

        /**
         * 解析方法参数为 Map。
         *
         * <p>特殊规则：
         * <ul>
         *   <li>{@link SqlParamProvider} 参数 → 调用 {@code toParamMap(template)} 展开</li>
         *   <li>无 @Param 的 {@code Map} 参数 → 直接展开合并</li>
         *   <li>@Param 注解的参数 → 以注解值为 key</li>
         * </ul>
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private Map<String, Object> resolveParams(Method method, Object[] args, String template) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (args == null) return map;
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Param pa = parameters[i].getAnnotation(Param.class);
                if (pa != null) {
                    map.put(pa.value(), args[i]);
                } else if (args[i] instanceof SqlParamProvider) {
                    // FilterParam 等动态参数：自动根据 SQL 模板生成 flat Map
                    map.putAll(((SqlParamProvider) args[i]).toParamMap(template));
                } else if (args[i] instanceof Map) {
                    map.putAll((Map) args[i]);
                } else if (isSimpleValue(args[i])) {
                    map.put(parameters[i].getName(), args[i]);
                } else {
                    // POJO → 反射 getter 展开为 Map，key = 属性名，value = 属性值
                    flattenPojo(args[i], map);
                }
            }
            return map;
        }

        private Class<?> resolveElementType(Type genericType) {
            if (genericType instanceof ParameterizedType pt) {
                Type[] args = pt.getActualTypeArguments();
                if (args.length > 0 && args[0] instanceof Class<?> c) return c;
            }
            return Map.class;
        }

        private void log(String sql, Map<String, Object> params) {
            if (properties.isLogging()) {
                log.info("==> SQL : {}", sql);
                log.info("==> PARAMS: {}", params);
            }
        }

        /** 判断是否为简单值类型（不需展平的） */
        private boolean isSimpleValue(Object obj) {
            if (obj == null) return true;
            Class<?> c = obj.getClass();
            return c == String.class
                    || c == Integer.class || c == Long.class || c == Double.class
                    || c == Float.class || c == Short.class || c == Byte.class
                    || c == Boolean.class || c == Character.class
                    || c == BigDecimal.class || c == BigInteger.class
                    || c == LocalDateTime.class || c == LocalDate.class
                    || c == LocalTime.class
                    || c.isEnum()
                    || c.isArray()
                    || obj instanceof Iterable;
        }

        /** 将 POJO 对象的属性展平到 Map（key = 属性名） */
        private void flattenPojo(Object obj, Map<String, Object> target) {
            try {
                for (PropertyDescriptor pd : Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors()) {
                    String name = pd.getName();
                    if ("class".equals(name)) continue;
                    Method getter = pd.getReadMethod();
                    if (getter != null) {
                        target.put(name, getter.invoke(obj));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("无法展平 POJO 参数: " + obj.getClass().getName(), e);
            }
        }
    }
}
