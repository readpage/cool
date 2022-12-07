package cn.undraw.handler.xss;


import cn.undraw.util.filter.JsoupUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author readpage
 * @create 2022-10-26 19:36
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        body = JsoupUtils.filter(sb.toString());
    }

    /**
     * 对header处理
     * @param name
     * @return
     */
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return JsoupUtils.filter(value);
    }

    /**
     * 对参数处理
     * @param name
     * @return
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return JsoupUtils.filter(value);
    }

    /**
     * 对数值进行处理
     * @param name
     * @return
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] arr = super.getParameterValues(name);
        if(arr != null){
            for (int i=0; i<arr.length; i++) {
                arr[i] = JsoupUtils.filter(arr[i]);
            }
        }
        return arr;
    }

    /**
     * 对 application/x-www-form-urlencoded 格式的POST请求参数，进行 cleanXSS解析
     * @return cleanXSS解析后的参数
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> values = super.getParameterMap();
        if (values == null) {
            return null;
        }

        Map<String, String[]> result = new HashMap<>();
        for (String key : values.keySet()) {
            int count = values.get(key).length;
            String[] encodedValues = new String[count];
            for (int i = 0; i < count; i++) {
                encodedValues[i] = JsoupUtils.filter(values.get(key)[i]);
            }
            result.put(key, encodedValues);
        }
        return result;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes());

        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) { }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
}
