package com.undraw.config;

import com.undraw.filter.AuthFilter;
import jakarta.annotation.Resource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author readpage
 * @date 2022-10-26 20:19
 */
@Configuration
public class FilterConfig {
    @Resource
    private AuthFilter authFilter;

    @Bean
    public FilterRegistrationBean webFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(authFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
