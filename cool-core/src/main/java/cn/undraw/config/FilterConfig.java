package cn.undraw.config;

import cn.undraw.handler.xss.XSSFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * @author readpage
 * @date 2022-10-26 20:19
 */

@Configuration
public class FilterConfig {
    @Autowired
    private XSSFilter xssFilter;

    @Bean
    public FilterRegistrationBean<XSSFilter> filterSample() {
        FilterRegistrationBean<XSSFilter> registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(xssFilter);
        ArrayList<String> urls = new ArrayList<>();
        //配置过滤规则
        urls.add("/*");
        registrationBean.setUrlPatterns(urls);
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
