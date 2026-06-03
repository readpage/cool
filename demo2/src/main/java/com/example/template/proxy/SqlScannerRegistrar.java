package com.example.template.proxy;

import com.example.template.annotation.SqlDao;
import com.example.template.annotation.SqlScan;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;

/** 扫描 @SqlDao 接口，注册 SqlFactoryBean。由 @SqlScan 触发。 */
public class SqlScannerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata meta, BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = meta.getAnnotationAttributes(SqlScan.class.getName());
        if (attrs == null) return;
        String[] packages = (String[]) attrs.get("value");

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition bd) {
                        return bd.getMetadata().isInterface();
                    }
                };
        scanner.addIncludeFilter(new AnnotationTypeFilter(SqlDao.class));

        for (String pkg : packages) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(pkg)) {
                try {
                    Class<?> daoClass = Class.forName(candidate.getBeanClassName());
                    String bn = uncapitalize(daoClass.getSimpleName());
                    if (registry.containsBeanDefinition(bn)) continue;
                    BeanDefinitionBuilder bd = BeanDefinitionBuilder
                            .genericBeanDefinition(SqlFactoryBean.class);
                    bd.addConstructorArgValue(daoClass);
                    registry.registerBeanDefinition(bn, bd.getBeanDefinition());
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("DAO load fail: " + candidate.getBeanClassName(), e);
                }
            }
        }
    }

    private static String uncapitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
