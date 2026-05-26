package com.example.template.proxy;

import com.example.template.QueryProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** 为 @SqlDao 接口创建动态代理的 FactoryBean（懒加载）。 */
public class SqlFactoryBean implements FactoryBean<Object>, ApplicationContextAware {

    private final Class<?> daoInterface;
    private ApplicationContext ctx;
    private Object proxyInstance;

    public SqlFactoryBean(Class<?> daoInterface) { this.daoInterface = daoInterface; }

    @Override
    public Object getObject() {
        if (proxyInstance == null) {
            NamedParameterJdbcTemplate jdbc = ctx.getBean(NamedParameterJdbcTemplate.class);
            QueryProperties props = ctx.getBean(QueryProperties.class);
            proxyInstance = QueryProxyFactory.create(daoInterface, jdbc, props);
        }
        return proxyInstance;
    }

    @Override
    public Class<?> getObjectType() { return daoInterface; }

    @Override
    public void setApplicationContext(ApplicationContext ctx) { this.ctx = ctx; }
}
