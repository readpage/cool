package cn.undraw.util.bean;

import cn.undraw.util.StrUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;

/**
 * @author readpage
 * @date 2023-03-06 23:50
 */
public class AnnoUtils {


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

    static Object invokeAnnotationMethod(Method method, Object annotation) {
        if (annotation == null) {
            return null;
        } else {
            try {
                if (Proxy.isProxyClass(annotation.getClass())) {
                    InvocationHandler handler = Proxy.getInvocationHandler(annotation);
                    return handler.invoke(annotation, method, (Object[])null);
                }
                return method.invoke(annotation, new Object[0]);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取字段注解的值
     *
     * @param annotation  注解对象
     * @param attributeName  注解属性名称
     * @return
     */
    public static Object getValue(Annotation annotation, String attributeName) {
        if (annotation != null) {
            try {
                Method method = annotation.annotationType().getDeclaredMethod(StrUtils.isNull(attributeName, "value"));
                return invokeAnnotationMethod(method, annotation);
            }  catch (NoSuchMethodException var3) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取类注解的值
     *
     * @param objClass      类class
     * @param annoClass     注解class
     * @param attributeName 注解属性名称
     * @return 返回T泛型类型
     */
    public static Object getValueByClass(Class<?> objClass, Class<? extends Annotation> annoClass, String attributeName) {
        Annotation annotation = objClass.getAnnotation(annoClass);
        return getValue(annotation, attributeName);
    }

    /**
     * 获取类注解的值
     *
     * @param objClass  类class
     * @param annoClass 注解class
     * @return 返回T泛型类型
     */
    public static Object getValueByClass(Class<?> objClass, Class<? extends Annotation> annoClass) {
        return getValueByClass(objClass, annoClass, null);
    }

    /**
     * 获取字段注解的值
     * @param field
     * @param annoClass 注解class
     * @param attributeName 注解属性名称
     * @return
     */
    public static Object getValueByField(Field field, Class<? extends Annotation> annoClass, String attributeName) {
        if (field != null) {
            Annotation annotation = field.getAnnotation(annoClass);
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
    public static Object getValueByField(Field field, Class<? extends Annotation> annoClass) {
        return getValueByField(field, annoClass, "value");
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
    public static <T> void setValue(Field field, Class<? extends Annotation> annoClass, String annoFieldName, T t) {
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

    @Deprecated
    public static <T> void setValue(Field field, Class<? extends Annotation> annoClass, T t) {
        setValue(field, annoClass, null, t);
    }


}
