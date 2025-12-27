package cn.undraw.handler.xss;

import cn.undraw.util.servlet.ServletUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/**
 * @author readpage
 * @date 2022-10-26 20:10
 */
@Component
public class XSSFilter implements Filter {

    FilterConfig filterConfig = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Value("${cool-core.filter.include:}")
    private String include;

    @Value("${cool-core.filter.exclude:}")
    private String exclude;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;


        // 是否开启过滤
        boolean flag = true;

        if ("".equals(include)) {
            flag = false;
        }

        if (ServletUtils.isMultipart(request)) {
            flag = false;
        }

        if (!"".equals(exclude)) {
            String[] split = exclude.split(", ");
            AntPathMatcher matcher = new AntPathMatcher();
            String uri = request.getRequestURI();
            for (String str : split) {
                if (matcher.match(str.trim(), uri)) {
                    flag = false;
                }
            }
        }

        if (flag) {
            filterChain.doFilter(new XssHttpServletRequest(request), servletResponse);
        } else {
            filterChain.doFilter(request, servletResponse);
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

}
