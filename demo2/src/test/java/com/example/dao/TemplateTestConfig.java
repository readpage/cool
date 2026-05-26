package com.example.dao;

import com.example.template.annotation.SqlScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 测试专用配置 —— 排除了 cool-core 和 Demo2Application 的组件扫描，
 * 只加载 template + dao 相关 bean。
 */
@Configuration
@EnableAutoConfiguration
@SqlScan("com.example.dao")
@ComponentScan(
    basePackages = "com.example.template",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "cn\\.undraw\\..*"
    )
)
public class TemplateTestConfig {
}
