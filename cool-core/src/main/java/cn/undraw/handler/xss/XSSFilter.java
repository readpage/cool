package cn.undraw.handler.xss;

import cn.undraw.util.servlet.ServletUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
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

        if (ServletUtils.isMultipart(request)) {
            // 判断类型如果是multipart类型，则将request手动封装为multipart类型。
            request = new StandardServletMultipartResolver().resolveMultipart(request);
        }

        // 是否开启过滤
        boolean flag = true;

        if ("".equals(include)) {
            flag = false;
        }

        if (!"".equals(exclude)) {
            String[] split = exclude.split(",");
            AntPathMatcher matcher = new AntPathMatcher();
            String uri = request.getRequestURI();
            for (String str : split) {
                if (matcher.match(str, uri)) {
                    flag = false;
                }
            }
        }

        if (flag) {
            filterChain.doFilter(new XssHttpServletRequestWrapper(request), servletResponse);
        } else {
            filterChain.doFilter(request, servletResponse);
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

}
