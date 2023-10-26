package cn.undraw.util.servlet;

import cn.undraw.handler.xss.XssHttpServletRequestWrapperFilter;
import cn.undraw.util.ConvertUtils;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 客户端工具类
 * @author readpage
 * @date 2022-11-29 19:28
 */
public class ServletUtils {
    /**
     * @param request 请求
     * @return java.lang.String
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) return "未知";
        //解析agent字符串
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        //获取浏览器对象
        Browser browser = userAgent.getBrowser();
        //获取操作系统对象
        OperatingSystem os = userAgent.getOperatingSystem();
        return browser.getName() + " | " + os.getName();
    }

    /**
     * userAgent
     * @return java.lang.String
     */
    public static String getUserAgent() {
        return getUserAgent(getRequest());
    }

    /**
     * 获得请求
     * @return HttpServletRequest
     */
    public synchronized static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 获取请求的参数
     * @param request
     * @return java.lang.String
     */
    public static String getParams(ServletRequest request) {
        return ConvertUtils.toJson(request.getParameterMap());
    }

    /**
     * 获取请求体
     * @param request
     * @return java.lang.String
     */
    public static String getBody(ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        if (request instanceof XssHttpServletRequestWrapperFilter) {
            try {
                br = request.getReader();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return sb.toString();
    }

    public static boolean isMultipart(ServletRequest request) {
        if (request == null) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            return true;
        }
        return false;
    }

    public static boolean isNotMultipart(ServletRequest request) {
        return !isMultipart(request);
    }

}
