package cn.undraw.handler.xss;

import cn.undraw.util.servlet.ServletUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author readpage
 * @date 2022-10-26 20:10
 *
 */

@Component
public class XSSFilter implements Filter {

    FilterConfig filterConfig = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        if (ServletUtils.isMultipart(request)) {
            // 判断类型如果是multipart类型，则将request手动封装为multipart类型。
            request = new StandardServletMultipartResolver().resolveMultipart(request);
        }
        filterChain.doFilter(new XssHttpServletRequestWrapper(request), servletResponse);
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

}
