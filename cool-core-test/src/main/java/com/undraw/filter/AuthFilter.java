package com.undraw.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * @author readpage
 * @date 2023-08-30 16:33
 */
@Slf4j
@Component
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        log.info(request.getRequestURI());
        // 继续执行后续的请求处理
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
