package cn.undraw.util;

import cn.undraw.util.result.ResultEnum;
import cn.undraw.util.result.ResultUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author readpage
 * @create 2022-11-02 9:56
 */

@Component
public class ConvertUtils {

    private static ObjectMapper mapper;
    @Autowired
    private ObjectMapper objectMapper;
    @PostConstruct
    public void init(){
        mapper  = objectMapper;
    }


    /**
     * 发送json数据
     * @param response
     * @param data
     */
    public static void sendJson(HttpServletResponse response, Object data) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.write(toJson(data));
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 对象转换json
     * @param obj
     * @return
     * @param <T>
     */
    public static<T> String toJson(T obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static<T> String toJson(ResultEnum resultEnum) {
        return toJson(ResultUtils.ok(resultEnum));
    }

    /**
     * json转换对象
     * @param json
     * @param valueType
     * @return
     * @param <T>
     */
    public static<T> T toObject(String json, Class<T> valueType) {
        if (json.isEmpty()) {
            return null;
        }
        try {
            return (T)mapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * json转换对象, 提供泛型类型支持
     * @param json
     * @param valueTypeRef
     * @return
     * @param <T>
     */
    public static<T> T toObject(String json, TypeReference<T> valueTypeRef) {
        if (json.isEmpty()) {
            return null;
        }
        try {
            return (T)mapper.readValue(json, valueTypeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * obj转换为map
     * @param obj
     * @return
     */

    public static Map<String, Object> toMap(Object obj) {
        return toObject(toJson(obj), Map.class);
    }

    /**
     * 深拷贝
     * @param o
     * @param valueType
     * @return
     * @param <T>
     */
    public static<T> T cloneDeep(T o, Class<T> valueType) {
        return toObject(toJson(o), valueType);
    }

    /**
     * 深拷贝 支持泛型类型
     * @param o
     * @param valueTypeRef
     * @return
     * @param <T>
     */
    public static<T> T cloneDeep(T o, TypeReference<T> valueTypeRef) {
        return toObject(toJson(o), valueTypeRef);
    }


    public static String toString(Object o) {
        return String.valueOf(o);
    }

    public static Integer toInteger(Object o) {
        return Integer.valueOf(toString(o));
    }

    public static Double toDouble(Object o) {
        return Double.valueOf(toString(o));
    }

    public static BigDecimal toBigDecimal(Object o) {
        return new BigDecimal(toString(o));
    }

}
