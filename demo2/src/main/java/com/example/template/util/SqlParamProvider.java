package com.example.template.util;

import java.util.Map;

/**
 * 动态参数提供者接口。
 *
 * <p>实现此接口的类可直接作为 DAO 方法参数，代理会自动调用 {@link #toParamMap(String)}
 * 并将返回的 Map 条目展开合并到渲染上下文中。
 */
public interface SqlParamProvider {

    /**
     * 根据 SQL 模板生成参数 Map（含 {@code {{filter}}} / {@code {{sort}}} 占位符值
     * 及 :column_N 命名参数）。
     *
     * @param sqlTemplate 与 {@code @Query} 注解完全一致的 SQL 模板字符串
     * @return 展开后的参数 Map
     */
    Map<String, Object> toParamMap(String sqlTemplate);
}
