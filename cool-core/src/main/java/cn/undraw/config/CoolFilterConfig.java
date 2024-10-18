package cn.undraw.config;

import cn.undraw.handler.xss.XSSFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * @author readpage
 * @date 2022-10-26 20:19
 */
@Configuration
public class CoolFilterConfig {
    @Autowired
    private XSSFilter xssFilter;

    @Value("${cool-core.filter.include:}")
    private String include;

    @Bean(name="coolFilterRegistrationBean")
    public FilterRegistrationBean<XSSFilter> webFilter() {
        FilterRegistrationBean<XSSFilter> registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(xssFilter);
        ArrayList<String> urls = new ArrayList<>();
        //配置过滤规则
        if (!"".equals(include)) {
            String[] split = include.split(", ");
            for (String str : split) {
                if (str.endsWith("**")) {
                    str = str.substring(0, str.length() - 1);
                }
                urls.add(str);
            }
            registrationBean.setUrlPatterns(urls);
            registrationBean.setOrder(1);
        }
        return registrationBean;
    }
}
