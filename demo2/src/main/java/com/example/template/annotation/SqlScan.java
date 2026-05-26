package com.example.template.annotation;

import com.example.template.proxy.SqlScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 启用自动扫描 @SqlDao 接口并注册为 Spring Bean。 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(SqlScannerRegistrar.class)
public @interface SqlScan {
    String[] value();
}
