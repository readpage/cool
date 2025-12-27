package cn.undraw.util.bean;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.StrUtils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author readpage
 * @date 2025-02-15 10:42
 */
public class BeanUtils {

    public static <T> Class<T> getClass(String className) {
        try {
            return (Class<T>)Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static<T> T getConstructor(String className) {
        return getConstructor(getClass(className));
    }

    /**
     * 根据类类型实例化对象
     * @return void
     */
    public static<T> T getConstructor(Class<T> clazz) {
        if (StrUtils.isEmpty(clazz)) {
            throw new IllegalArgumentException();
        }
        try {
            Constructor defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return (T) defaultConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
        if (columns == null || columns.length == 0) {
            return null;
        }
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
            Method method = getMethod(obj.getClass(), methodName);
            Type[] paramTypes = method.getGenericParameterTypes();
            Object[] objects = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                Type paramType = paramTypes[i];
                objects[i] = ConvertUtils.cloneDeep(params[0], getClass(paramType.getTypeName()));
            }
            method.setAccessible(true);
            return method.invoke(obj, objects);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodError(methodName));
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        if (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取class的所有属性
     * @param clazz
     * @return java.util.List<java.lang.reflect.Field>
     */
    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        fields.addAll(Arrays.asList(declaredFields));

        // 获取父类的字段
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null) {
            Field[] superFields = superclass.getDeclaredFields();
            fields.addAll(Arrays.asList(superFields));
            superclass = superclass.getSuperclass();
        }

        return fields;
    }

    public static List<String> getFieldNames(Class<?> clazz) {
        List<String> list = new ArrayList();

        List<Field> fields = getFields(clazz);

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            list.add(fieldName);
        }
        return list;
    }

    /**
     * 获取对象的属性名称和值
     * @param obj
     * @return
     */
    public static Map<String, Object> getEntry(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
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
            List<Field> fields = getFields(sourceClass);

            for (Field field : fields) {
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

    /**
     * 单个对象属性复制
     * 两个对象将前者拷贝给后者 <p>如果两个类不一样时，只会拷贝属性一样的内容</p>
     * @param o 复制源
     * @return T
     */
    public static <T> T copy(T o) {
        if (StrUtils.isEmpty(o)) {
            return null;
        }
        T t = (T) getConstructor(o.getClass());
        org.springframework.beans.BeanUtils.copyProperties(o, t);
        return t;
    }

    /**
     * 对象列表属性复制
     * @param list 复制源
     * @return T
     */
    public static <T> List<T> copyList(List<T> list) {
        List<T> newList = new ArrayList<>();
        if (list == null) {
            return newList;
        }
        for (T t : list) {
            newList.add(copy(t));
        }
        return newList;
    }

    /**
     * 对象转换map
     * @param obj
     * @return
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        Map map = new LinkedHashMap();
        if (obj == null) {
            return map;
        }
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                map.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    /**
     * map转对象
     * @param map
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        try {
            T object = clazz.newInstance(); // 创建对象实例
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // 设置私有字段可访问
                String fieldName = field.getName();
                if (map.containsKey(fieldName)) {
                    field.set(object, map.get(fieldName)); // 设置字段值
                }
            }
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
