package cn.undraw.util.bean;

import cn.undraw.util.StrUtils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author readpage
 * @date 2025-02-15 10:42
 */
public class BeanUtils {


    /**
     * 获取属性名称
     * @param fn
     * @return
     * @param <T>
     */
    public static <T> String getFieldName(SFunction<T, ?> fn) {
        SerializedLambda lambda = null;
        try {
            Method method = null;
            method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            lambda = (SerializedLambda) method.invoke(fn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 获取方法名
        String methodName = lambda.getImplMethodName();

        return methodToProperty(methodName);
    }

    /**
     * 从方法名获取属性名
     * @param methodName
     * @return
     */
    private static String methodToProperty(String methodName) {
        String prefix = null;
        if (methodName.startsWith("get")) {
            prefix = "get";
        } else if(methodName.startsWith("is")) {
            prefix = "is";
        } else if (prefix == null) {
            return null;
        }
        return StrUtils.toLowerCaseFirstOne(methodName.replace(prefix, ""));
    }

    public static <T> String getFieldName(List<SFunction<T, ?>> columns) {
        return columns.stream().map((o) -> getFieldName(o)).collect(Collectors.joining(","));
    }

    public static <T> String getFieldName(SFunction<T, ?> ...columns) {
        return getFieldName(Arrays.asList(columns));
    }


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


    /**
     * 合并对象
     * @param source 原来值
     * @param target 目标值
     */
    public static void merge(Object source, Object target) {
        try {
            Class<?> sourceClass = source.getClass();
            Class<?> targetClass = target.getClass();
            Field[] sourceFields = sourceClass.getDeclaredFields();
            for (Field field : sourceFields) {
                field.setAccessible(true); // 确保可以访问私有字段
                if (field.get(source) != null) { // 如果源对象中的字段非null
                    try {
                        Field targetField = targetClass.getDeclaredField(field.getName());
                        if (!Modifier.isStatic(targetField.getModifiers())) {
                            targetField.setAccessible(true);
                            targetField.set(target, field.get(source)); // 将值复制到目标对象
                        }
                    } catch (NoSuchFieldException e) {
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <T> boolean equals(Object v1, Object v2, SFunction<T, ?> ...columns) {
        List<String> list = StrUtils.toList(getFieldName(columns), ",");
        return equals(v1, v2, list);
    }

    public static <T> boolean equals(Object v1, Object v2, List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            Object fieldValue1 = BeanUtils.getFieldValue(v1, fieldName);
            Object fieldValue2 = BeanUtils.getFieldValue(v2, fieldName);
            if (!Objects.equals(fieldValue1, fieldValue2)) {
                return false;
            }
        }
        return true;
    }

}
