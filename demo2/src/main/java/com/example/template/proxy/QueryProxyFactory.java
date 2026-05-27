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

import java.lang.reflect.*;
import java.util.*;

/**
 * JDK 动态代理工厂 —— 拦截 @Query / @Modify 注解的方法，编译模板并执行 SQL。
 */
public class QueryProxyFactory {

    private static final Logger log = LoggerFactory.getLogger(QueryProxyFactory.class);

    private final SqlTemplateEngine engine;
    private final NamedParameterJdbcTemplate jdbc;
    private final QueryProperties properties;

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

            // 2. LIMIT 分页
            String limitSql = renderedSql + " LIMIT " + size + " OFFSET " + (current - 1) * size;
            Class<?> elementType = resolveElementType(method.getGenericReturnType());
            @SuppressWarnings("unchecked")
            List<T> records = (List<T>) jdbc.query(limitSql, params,
                    new BeanPropertyRowMapper<>(elementType));

            return new PageResult<>(total != null ? total : 0, current, size, records);
        }

        /** 从 SQL 截取 COUNT：移除 ORDER BY，SELECT ... FROM → SELECT COUNT(*) FROM */
        private String buildCountSql(String renderedSql) {
            String sql = renderedSql.replaceAll("(?i)\\s+ORDER\\s+BY\\s+.*$", "");
            return sql.replaceAll("(?i)^\\s*SELECT\\s+.+?\\s+FROM\\s+", "SELECT COUNT(*) FROM ");
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
                } else {
                    map.put(parameters[i].getName(), args[i]);
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
    }
}
