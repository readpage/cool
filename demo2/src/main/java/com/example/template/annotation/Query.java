package com.example.template.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义 SQL 模板查询注解。
 *
 * <pre>{@code
 * &#64;Query("SELECT * FROM {{table}} WHERE 1=1 [[AND name = #{name}]]")
 * List<User> findByCondition(@Param("table") String table, @Param("name") String name);
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {
    String value();
}
