package cn.undraw.util.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author readpage
 * @date 2023-03-06 23:50
 */
public class AnnoUtils {

    /**
     * 获取class的所有属性
     * @param objClass
     * @return java.util.List<java.lang.reflect.Field>
     */
    @Deprecated
    public static List<Field> getFields(Class<?> objClass) {
        List<Field> fields = new ArrayList<>();
        try {
            Field[] declaredFields = objClass.getDeclaredFields();
            if (declaredFields != null) {
                fields = Arrays.stream(declaredFields).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fields;
    }


    /**
     * 判断class是否存在某个注解
     *
     * @param objClass  类class
     * @param annoClass 注解class
     * @return boolean true 存在，false 不存在
     */
    public static boolean isExistAnno(Class<?> objClass, Class<? extends Annotation> annoClass) {
        return objClass.isAnnotationPresent(annoClass);
    }


    /**
     * 判断属性(Field)是否存在某个注解
     *
     * @param field     类属性
     * @param annoClass 注解class
     * @return boolean true 存在，false 不存在
     */
    public static boolean isExistAnno(Field field, Class<? extends Annotation> annoClass) {
        return field.isAnnotationPresent(annoClass);
    }


    /**
     * 获取类注解的值
     *
     * @param objClass  类class
     * @param annoClass 注解class
     * @return 返回T泛型类型
     */
    public static <T> T getAnnoValueByClass(Class<?> objClass, Class<? extends Annotation> annoClass) {
        return getAnnoValueByClass(objClass, annoClass, null);
    }


    /**
     * 获取类注解的值
     *
     * @param objClass      类class
     * @param annoClass     注解class
     * @param annoFieldName 注解属性名称
     * @return 返回T泛型类型
     */
    public static <T> T getAnnoValueByClass(Class<?> objClass, Class<? extends Annotation> annoClass, String annoFieldName) {
        Annotation annotation = objClass.getAnnotation(annoClass);
        T t = null;
        try {
            if (annotation != null) {
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
                Field field = invocationHandler.getClass().getDeclaredField("memberValues");
                field.setAccessible(true);
                Map<String, Object> memberValues = (Map<String, Object>) field.get(invocationHandler);
                t = (T) memberValues.get(Optional.ofNullable(annoFieldName).orElse("value"));
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    /**
     * 获取字段注解的值
     * @param objClass  类class
     * @param annoClass 注解class
     * @return
     */
    public static Object getValue(Class<?> objClass, Class<? extends Annotation> annoClass) {
        return getValue(objClass, annoClass, "value");
    }

    /**
     * 获取字段注解的值
     * @param objClass  类class
     * @param annoClass 注解class
     * @return
     */
    public static Object getValue(Class<?> objClass, Class<? extends Annotation> annoClass, String attributeName) {
        if (objClass != null) {
            Annotation annotation = objClass.getAnnotation(annoClass);
            return getValue(annotation, attributeName);
        }
        return null;
    }


    /**
     * 获取字段注解的值
     * @param field
     * @param annoClass 注解class
     * @return
     */
    public static Object getValue(Field field, Class<? extends Annotation> annoClass) {
        return getValue(field, annoClass, "value");
    }

    /**
     * 获取字段注解的值
     * @param field
     * @param annoClass 注解class
     * @param attributeName 注解属性名称
     * @return
     */
    public static Object getValue(Field field, Class<? extends Annotation> annoClass, String attributeName) {
        if (field != null) {
            Annotation annotation = field.getAnnotation(annoClass);
            return getValue(annotation, attributeName);
        }
        return null;
    }


    /**
     * 获取字段注解的值
     *
     * @param annotation  注解对象
     * @param attributeName  注解属性名称
     * @return
     */
    public static Object getValue(Annotation annotation, String attributeName) {
        try {
            if (annotation != null) {
                InvocationHandler handler = Proxy.getInvocationHandler(annotation);
                for(Method method : annotation.annotationType().getDeclaredMethods()) {
                    if (method.getName().equals(attributeName) && method.getParameterCount() == 0) {
                        return handler.invoke(annotation, method, new Object[]{"test"});
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    @Deprecated
    public static <T> void setAnnoValueByField(Field field, Class<? extends Annotation> annoClass, T t) {
        setAnnoValueByField(field, annoClass, null, t);
    }


    /**
     * 设置字段注解的值
     *
     * @param field         字段对象
     * @param annoClass     注解class
     * @param annoFieldName 注解属性名称
     * @param t             值
     */
    @Deprecated
    public static <T> void setAnnoValueByField(Field field, Class<? extends Annotation> annoClass, String annoFieldName, T t) {
        Annotation annotation = field.getAnnotation(annoClass);
        try {
            if (annotation != null) {
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
                Field f = invocationHandler.getClass().getDeclaredField("memberValues");
                f.setAccessible(true);
                Map<String, Object> memberValues = (Map<String, Object>) f.get(invocationHandler);
                memberValues.put(Optional.ofNullable(annoFieldName).orElse("value"), t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
