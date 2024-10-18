package cn.undraw.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectUtils {

    /**
     * 获取指定对象的指定字段值
     * @param obj 对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 设置指定对象的指定字段值
     * @param obj 对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 调用指定对象的指定方法
     * @param obj 对象
     * @param methodName 方法名
     * @param params 参数
     * @return 方法返回值
     */
    public static Object invokeMethod(Object obj, String methodName, Object... params) {
        try {
            Class<?>[] paramTypes = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getClass();
            }
            Method method = obj.getClass().getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(obj, params);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 获取class的所有属性
     * @param objClass
     * @return java.util.List<java.lang.reflect.Field>
     */
    public static List<Field> getFields(Class<?> objClass) {
        List<Field> fields = new ArrayList<>();
        try {
            Field[] declaredFields = objClass.getDeclaredFields();
            if (declaredFields != null) {
                fields = Arrays.stream(declaredFields).collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return fields;
    }

    /**
     * 获取对象的属性名称和值
     * @param obj
     * @return
     */
    public static Map<String, Object> getEntry(Object obj) {
        Map<String, Object> entryMap = new HashMap<>();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(obj);
                entryMap.put(fieldName, fieldValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return entryMap;
    }
    /**
     * 获取指定类的所有字段名和类型的映射
     * @param clazz 类
     * @return 字段映射
     */
    public static Map<String, Class<?>> getFieldMap(Class<?> clazz) {
        Map<String, Class<?>> fieldMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field.getType());
        }
        return fieldMap;
    }
}
