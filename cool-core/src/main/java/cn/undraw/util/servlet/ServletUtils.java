package cn.undraw.util.servlet;

import cn.undraw.handler.xss.XssHttpServletRequest;
import cn.undraw.util.ConvertUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 客户端工具类
 * @author readpage
 * @date 2022-11-29 19:28
 */
public class ServletUtils {

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

    public static String getParams() {
        return getParams(getRequest());
    }


    /**
     * 获取请求体
     * @param request
     * @return java.lang.String
     */
    public static String getBody(ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        if (request instanceof XssHttpServletRequest) {
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


    public static String getBody() {
        return getBody(getRequest());
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
