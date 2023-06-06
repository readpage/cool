package com.undraw.config;

import cn.undraw.util.filter.SensitiveUtils;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * @author readpage
 * @date 2023-05-24 11:01
 */
@Configuration
public class InitConfig {
    @PostConstruct
    public void init() {
        List<String> filters = Arrays.asList("李明", "张三");
        filters.forEach(t -> SensitiveUtils.add(t));
    }
}
