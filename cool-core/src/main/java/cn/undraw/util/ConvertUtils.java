package cn.undraw.util;

import cn.undraw.util.result.R;
import cn.undraw.util.result.ResultEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author readpage
 * @date 2022-11-02 9:56
 */

@Component
public class ConvertUtils {

    private static ObjectMapper mapper = new ObjectMapper();
    @Resource
    private ObjectMapper objectMapper;

    // @PostConstruct: 加该注解会在项目启动的时候执行该方法，也可以理解为在spring容器初始化的时候执行该方法。
    @PostConstruct
    public void init(){
        mapper = objectMapper;
    }


    /**
     * 发送json格式数据
     * @param response
     * @param data
     * @return void
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
     * 深拷贝
     * @param o
     * @param valueType
     * @return T
     */
    public static<T> T cloneDeep(Object o, Class<T> valueType) {
        String str;
        if (o instanceof String) {
            str = (String)o;
        } else {
            str = toJson(o);
        }
        return toObject(str, valueType);
    }

    /**
     * 深拷贝 支持泛型类型
     * @param o
     * @param valueTypeRef
     * @return T
     */
    public static<T> T cloneDeep(Object o, TypeReference<T> valueTypeRef) {
        String str;
        if (o instanceof String) {
            str = (String)o;
        } else {
            str = toJson(o);
        }
        return toObject(str, valueTypeRef);
    }

    /**
     * Collection
     * @param sources 源对象列表
     * @param clazz 目标对象类型
     * @return java.util.Collection<T>
     */
    public static<T, U> List<T> copy(Collection<U> sources, Class<T> clazz) {
        if (StrUtils.isEmpty(sources)) {
            return null;
        }
        if (StrUtils.isEmpty(clazz)) {
            throw new IllegalArgumentException();
        }
        return sources.stream().map(e -> copy(e, clazz)).collect(Collectors.toList());
    }

    /**
     * 单个对象属性复制
     * 两个对象将前者拷贝给后者 <p>如果两个类不一样时，只会拷贝属性一样的内容</p>
     * @param source 复制源
     * @param clazz 目标对象类型
     * @return T
     */
    public static<T> T copy(Object source, Class<T> clazz) {
        if (StrUtils.isEmpty(source)) {
            return null;
        }
        if (StrUtils.isEmpty(clazz)) {
            throw new IllegalArgumentException();
        }
        T t = AnnoUtils.getConstructor(clazz);
        BeanUtils.copyProperties(source, t);
        return t;
    }

    /**
     * 对象转换json
     * @param obj
     * @return java.lang.String
     */
    public static String toJson(Object obj) {
        if (obj instanceof String) {
            return (String)obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Deprecated
    public static<T> String toJson(ResultEnum resultEnum) {
        if (resultEnum == null) {
            return null;
        }
        return toJson(R.ok(resultEnum));
    }

    /**
     * json转换对象
     * @param json
     * @param valueType
     * @return T
     */
    public static<T> T toObject(String json, Class<T> valueType) {
        if (json == null) {
            return null;
        }
        try {
            return mapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * json转换对象, 提供泛型类型支持
     * @param json
     * @param valueTypeRef
     * @return T
     */
    public static<T> T toObject(String json, TypeReference<T> valueTypeRef) {
        if (json == null) {
            return null;
        }
        try {
            return mapper.readValue(json, valueTypeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * obj转换为map
     * @param obj
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public static Map<String, Object> toMap(Object obj) {
        return toObject(toJson(obj), Map.class);
    }


    private static String toString(Object o) {
        return String.valueOf(o);
    }

    /**
     * 返回Integer类型,不是数字返回null
     * @param o
     * @return java.lang.Integer
     */
    public static Integer toInteger(Object o) {
        String s = toString(o);
        if (!StrUtils.isNumber(s)) return null;
        return Integer.valueOf(s);
    }

    /**
     * 返回Double类型,不是数字返回null
     * @param o
     * @return java.lang.Double
     */
    public static Double toDouble(Object o) {
        String s = toString(o);
        if (!StrUtils.isNumber(s)) return null;
        return Double.valueOf(s);
    }

    /**
     * 返回Long类型,不是数字返回null
     * @param o
     * @return java.lang.Long
     */
    public static Long toLong(Object o) {
        String s = toString(o);
        if (!StrUtils.isNumber(s)) return null;
        return Long.valueOf(s);
    }

    /**
     * 返回BigDecimal类型,不是数字返回null
     * @param o
     * @return java.math.BigDecimal
     */
    public static BigDecimal toBigDecimal(Object o) {
        String s = toString(o);
        if (!StrUtils.isNumber(s)) return null;
        return new BigDecimal(s);
    }

    public static Boolean toBoolean(Object o) {
        return Boolean.valueOf(toString(o));
    }

    /**
     *
     * @param list  要分页的集合
     * @param pageNum    第几页
     * @param pageSize  每页条数
     * @return      分页集合对象
     */
    public static List page(List list, int pageNum, int pageSize){

        List result = new ArrayList();
        if (list != null && list.size() > 0) {
            int allCount = list.size();
            int pageCount = (allCount + pageSize - 1) / pageSize;
            if (pageNum >= pageCount) {
                pageNum = pageCount;
            }
            int start = (pageNum - 1) * pageSize;
            int end = pageNum * pageSize;
            if (end >= allCount) {
                end = allCount;
            }
            for (int i = start; i < end; i++) {
                result.add(list.get(i));
            }
        }
        return (result.size() > 0) ? result : null;
    }

}
