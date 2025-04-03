package cn.undraw.handler.xss;

import cn.undraw.util.StrUtils;
import cn.undraw.util.filter.JsoupUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author readpage
 * @date 2022-10-26 19:36
 */
public class XssHttpServletRequest extends HttpServletRequestWrapper {

    private final String body;

    public XssHttpServletRequest(HttpServletRequest request) {
        super(request);
        StringBuilder sb = null;
        try (InputStream is = request.getInputStream()) {
            Reader in = new InputStreamReader(is, "UTF-8");
            sb = new StringBuilder();
            for (int n; (n = in.read()) != -1;)
            {
                sb.append((char) n);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        body = JsoupUtils.filter(String.valueOf(sb));
    }

    /**
     * 对header处理
     * @param name
     * @return java.lang.String
     */
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return JsoupUtils.filter(value);
    }

    /**
     * 对参数处理
     * @param name
     * @return java.lang.String
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return JsoupUtils.filter(value);
    }

    /**
     * 对数值进行处理
     * @param name
     * @return java.lang.String[]
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
     * @return java.util.Map<java.lang.String,java.lang.String[]>
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
    public ServletInputStream getInputStream() {
        byte[] bytes;
        if (StrUtils.isEmpty(body)) {
            bytes = "".getBytes();
        } else {
            bytes = body.getBytes();
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        return new ServletInputStream() {

            @Override
            public int read() {
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
