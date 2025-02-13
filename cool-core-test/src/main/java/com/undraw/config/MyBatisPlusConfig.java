package com.undraw.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.undraw.handler.CustomSqlInjector;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author readpage
 * @date 2023-04-24 17:17
 */
@Configuration
@MapperScan("com.undraw.mapper") // mybatis扫描mapper文件
public class MyBatisPlusConfig {
    /**
     *  fix : No MyBatis mapper was found in '[xx.mapper]' package. Please check your configuration
     */
    @Mapper
    public interface NoWarnMapper {
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public CustomSqlInjector customSqlInjector() {
        return new CustomSqlInjector();
    }
}
