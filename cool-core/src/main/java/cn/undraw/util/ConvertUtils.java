package cn.undraw.util;

import cn.undraw.util.result.R;
import cn.undraw.util.result.ResultEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author readpage
 * @date 2022-11-02 9:56
 */

@Component
public class ConvertUtils {

    private static ObjectMapper mapper = new ObjectMapper();
    @Resource
    private ObjectMapper objectMapper;

//    public static void config(ObjectMapper mapper) {
//        //设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//    }

//    static {
//        config(mapper);
//    }

    // @PostConstruct: 加该注解会在项目启动的时候执行该方法，也可以理解为在spring容器初始化的时候执行该方法。
    @PostConstruct
    public void init(){
        mapper = objectMapper;
//        config(mapper);
    }

    /**
     * 是否是有效的json
     * @param json
     * @return boolean
     */
    public static boolean isValidJSON(final String json) {
        if (StrUtils.isEmpty(json)) {
            return false;
        }
        boolean valid = true;
        try{
            mapper.readTree(json);
        } catch(JsonProcessingException e){
            valid = false;
        }
        return valid;
    }

    /**
     * 对象转换json
     * @param obj
     * @return java.lang.String
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
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

        if (valueType == String.class) {
            return (T)json;
        } else if (valueType == Integer.class) {
            return (T)toInteger(json);
        } else if (valueType == Long.class) {
            return (T)toLong(json);
        } else if (valueType == Double.class) {
            return (T)toDouble(json);
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
    public static<T, U> U cloneDeep(T o, Class<U> valueType) {
        String str;
        if (o == null) {
            return null;
        } else {
            str = toJson(o);
        }
        if (valueType == null) {
            valueType = (Class<U>) o.getClass();
        }
        return toObject(str, valueType);
    }


    /**
     * 深拷贝 支持泛型类型
     * @param o
     * @param valueTypeRef
     * @return T
     */
    public static<T, U> U cloneDeep(T o, TypeReference<U> valueTypeRef) {
        String str;
        if (o instanceof String) {
            str = (String)o;
        } else {
            str = toJson(o);
        }
        return toObject(str, valueTypeRef);
    }

    /**
     * 深拷贝
     * @param o
     * @return T
     */
    public static<T> T cloneDeep(T o) {
        return cloneDeep(o, (Class<T>) null);
    }

    /**
     * 深拷贝
     * @param list
     * @param valueType
     * @return T
     */
    public static<T, U> List<U> cloneDeep(Collection<T> list, Class<U> valueType) {
        if (StrUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        if (list.size() >= 1 && valueType == null) {
            Object firstElement = list.iterator().next();
            valueType = (Class<U>) firstElement.getClass();
        }
        TypeFactory typeFactory = mapper.getTypeFactory();
        JavaType javaType = typeFactory.constructParametricType(List.class, valueType);
        String json = toJson(list);
        try {
            return mapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 深拷贝
     * @param list
     * @return T
     */
    public static<T, U> List<U> cloneDeep(Collection<T> list) {
        return cloneDeep(list, (Class<U>) null);
    }

    /**
     * obj转换为map
     * @param obj
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public static Map<String, Object> toMap(Object obj) {
        return toObject(toJson(obj), Map.class);
    }


    public static String toString(Object o) {
        return o == null ? "" : o.toString();
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
    public static <T> List<T> page(List<T> list, int pageNum, int pageSize){

        List result = new ArrayList();
        if (list != null && list.size() > 0) {
            int allCount = list.size();
//            int pageCount = (allCount - 1) / pageSize + 1;
            int pageCount = (int)Math.ceil((double)allCount / pageSize);
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


    /**
     * 将一个大列表分割成指定大小的多个小列表。
     *
     * @param collection        要分割的列表
     * @param batchSize   每个批次的大小
     * @param <T>         列表元素的类型
     * @return            包含所有批次的列表
     */
    public static <T> List<List<T>> batchList(Collection<T> collection, int batchSize) {
        List list = new ArrayList(collection);
        if (list == null || batchSize <= 0) {
            throw new IllegalArgumentException("List或batchSize必须有效。");
        }

        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(new ArrayList<>(list.subList(i, end)));
        }
        return batches;
    }

    public static <T> List<List<T>> batchList(Collection<T> list) {
        return batchList(list, 1000);
    }


}
