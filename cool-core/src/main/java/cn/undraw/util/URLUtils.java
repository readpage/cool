package cn.undraw.util;

import cn.undraw.handler.exception.customer.CustomerException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author readpage
 * @date 2023-03-23 17:15
 */
public class URLUtils {

    public static String mapToUrlParams(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            try {
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                sb.append('=');
                sb.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    /**
     * 编码 汉字转换为%E4%BD%A0形式
     * @param str
     * @return java.lang.String
     */
    public static String encode(String str) {
        return encode(str, "UTF-8");
    }


    /**
     * 编码 汉字转换为%E4%BD%A0形式
     * @param str
     * @param charset
     * @return java.lang.String
     */
    public static String encode(String str, String charset) {
        if (StrUtils.isEmpty(str)) {
            return str;
        }
        // 匹配中文
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]+");
        Matcher m = p.matcher(str);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            try {
                m.appendReplacement(b, URLEncoder.encode(m.group(0), charset));
            } catch (UnsupportedEncodingException e) {
                throw new CustomerException(str + "字符串编码失败!");
            }
        }
        m.appendTail(b);
        return b.toString();
    }

    /**
     * 解码 将%E4%BD%A0转换为汉字 
     * @param str
     * @return
     */
    public static String decode(String str) {
        if (StrUtils.isEmpty(str)) {
            return str;
        }
        String result;
        try {
            result = URLDecoder.decode(str,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = "";
        }
        return result;
    }
}
