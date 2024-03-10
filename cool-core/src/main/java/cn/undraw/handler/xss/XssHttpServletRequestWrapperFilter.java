package cn.undraw.handler.xss;

import cn.undraw.util.StrUtils;
import cn.undraw.util.filter.JsoupUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author readpage
 * @date 2022-10-26 19:36
 */
public class XssHttpServletRequestWrapperFilter extends HttpServletRequestWrapper {

    private final String body;

    public XssHttpServletRequestWrapperFilter(HttpServletRequest request) throws IOException {
        super(request);
        InputStream is = null;
        StringBuilder sb = null;
        try {
            is = request.getInputStream();
            sb = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n; (n = is.read(b)) != -1;)
            {
                sb.append(new String(b, 0, n));
            }
        } finally {
            if(is != null) {
                is.close();
            }
        }
        body = JsoupUtils.filter(sb.toString());
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
    public ServletInputStream getInputStream() throws IOException {
        byte[] bytes;
        if (StrUtils.isEmpty(body)) {
            bytes = "".getBytes();
        } else {
            bytes = body.getBytes();
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

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
