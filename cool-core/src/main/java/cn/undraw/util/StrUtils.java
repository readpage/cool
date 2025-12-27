package cn.undraw.util;

import cn.undraw.model.Compare;
import cn.undraw.util.bean.BeanUtils;
import cn.undraw.util.bean.SFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author readpage
 * @date 2022-11-10 9:35
 */
@Slf4j
public class StrUtils {


    /**
     * 判断是否是数字
     * @param val
     * @return boolean
     */
    public static<T> boolean isNumber(T val) {
        try {
            if (val instanceof Number) {
                return true;
            }
            String s = String.valueOf(val);
            return s.matches("^[+-]?(\\d+|\\d+\\.\\d+)$") || s.matches("^[+-]?\\d+\\.?\\d*[Ee][+-]?\\d+$");
        } catch (Exception e){
            return false;
        }
    }

    /**
     * 判断是否为空, 如: null、""
     * @param obj
     * @return boolean
     */
    public static<T> boolean isEmpty(T obj) {
        if (null == obj) {
            return true;
        }

        if (obj instanceof String) {
            return ((String) obj).length() == 0;
        } else if (obj instanceof Collection) {
            return CollectionUtils.isEmpty((Collection<?>) obj);
        } else if (obj instanceof Map) {
            return ((Map)obj).isEmpty();
        } else if (obj instanceof Object[]) {
            return ((Object[])obj).length == 0;
        } else if (obj.getClass().isArray()) {
            if (obj instanceof long[]) {
                return ((long[]) obj).length == 0;
            } else if (obj instanceof int[]) {
                return ((int[]) obj).length == 0;
            } else if (obj instanceof short[]) {
                return ((short[]) obj).length == 0;
            } else if (obj instanceof char[]) {
                return ((char[]) obj).length == 0;
            } else if (obj instanceof byte[]) {
                return ((byte[]) obj).length == 0;
            } else if (obj instanceof double[]) {
                return ((byte[]) obj).length == 0;
            } else if (obj instanceof float[]) {
                return ((byte[]) obj).length == 0;
            } else if (obj instanceof boolean[]) {
                return ((byte[]) obj).length == 0;
            }
        }
        return false;
    }

    /**
     * 判断是否不能为空
     * @param obj
     * @return boolean
     */
    public static<T> boolean isNotEmpty(T obj) {
        return !isEmpty(obj);
    }

    /**
     * v1不为空则返回v1值,否则返回v2值
     * @date 2023-02-16 16:53
     * @param v1
     * @param v2
     * @return T
     */
    public static<T> T isNull(T v1, T v2) {
        return isNotEmpty(v1) ? v1 : v2;
    }
    /**
     * 两个参数比较，如果返回结果等于1，则返回第二个参数，否则返回第一个参数
     * @param val
     * @param val2
     * @param biFunction
     * @return T
     */
    @Deprecated
    public static<T> T isExceed(T val, T val2, BiFunction<T, T, Integer> biFunction) {
        return biFunction.apply(val, val2) == 1 ? val2: val;
    }

    /**
     * 两个参数比较，如果返回结果等于1，则返回第二个参数，否则返回第一个参数
     * @param val
     * @param val2
     * @return java.lang.Integer
     */
    @Deprecated
    public static Integer isExceed(Integer val, Integer val2) {
        return Integer.compare(val, val2) == 1 ? val2: val;
    }

    /**
     * 随机获取用户名
     * @return java.lang.String
     */
    public static String randomName() {
        ClassPathResource nameList = new ClassPathResource("/assets/username.txt");
        if (nameList == null) {
            throw new RuntimeException("读取username文件失败!");
        } else {
            List<String> list = new ArrayList<>();
            BufferedReader reader = null;
            try {
                InputStream is = nameList.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    list.add(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            Random random = new Random();
            int i = random.nextInt(list.size());
            return list.get(i);
        }
    }

    /**
     * 计算字符串在给定字符串出现的次数
     * @param src 数据源
     * @param des 目标值
     * @return int 出现的次数
     */
    public static int findCount(String src, String des) {
        int index = 0;
        int count = 0;
        while ((index = src.indexOf(des, index)) != -1) {
            count++;
            index += des.length();
        }
        return count;
    }


    /**
     * 提取字符串中的数字内容
     * @param v
     * @return
     */
    public static List<Long> findNumbers(String v) {
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(v);
        List<Long> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(ConvertUtils.toLong(matcher.group()));
        }
        return list;
    }

    /**
     * 将字符串数组转换为由指定分隔符分割的字符串
     * @param collection 输入数组
     * @param separator 指定分隔符
     * @return java.lang.String
     */
    public static<T> String join(Collection<T> collection, String separator) {
        if (StrUtils.isEmpty(collection)) return null;
        StringJoiner sj = new StringJoiner(separator);
        for (T e : collection) {
            sj.add(String.valueOf(e));
        }
        return sj.toString();
    }

    /**
     * 将字符串数组转换为由指定分隔符分割的字符串
     * @param arrStr 输入数组
     * @param separator 指定分隔符
     * @return java.lang.String
     */
    public static<T> String join(String[] arrStr, String separator) {
        if (arrStr == null) return null;
        return String.join(separator, arrStr);
    }

    /**
     * 将字符串按指定字符串分割存入到List中
     * @param str       字符串
     * @param separator 分隔符
     * @return java.util.List<java.lang.String>
     */
    public static List<String> toList(String str, String separator) {
        List<String> list = new ArrayList<>();
        if (StrUtils.isNotEmpty(str)) {
            if (str.contains(separator)) {
                list = new ArrayList<>(Arrays.asList(str.split(separator)));
            } else {
                list.add(str);
            }
        }

        return list;
    }

    /**
     * 删除字符串前缀
     * @param str
     * @param prefix
     * @return java.lang.String
     */
    public static String removePrefix(String str, String prefix) {
        if (isNotEmpty(str)) {
            return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
        }
        return str;
    }

    /**
     * 删除字符串后缀
     * @param str
     * @param suffix
     * @return java.lang.String
     */
    public static String removeSuffix(String str, String suffix) {
        if (isNotEmpty(str)) {
            return str.endsWith(suffix) ? str.substring(0, str.length() - suffix.length()) : str;
        }
        return str;
    }

    /**
     * 根据字节长度截取字符串
     * @param str 字符串
     * @param len 字节长度
     * @return java.lang.String
     */
    public static String substringByByte(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len <= 0) {
            return "";
        }
        byte[] bytes = str.getBytes();
        if (len >= bytes.length) {
            return str;
        }
        return new String(bytes, 0, len);
    }


    /**
     * 根据文字数量截取字符串
     * mysql: TEXT 可以存储最大 65535 个字符，而 LONGTEXT 可以存储最大 4294967295 个字符
     * @param str
     * @param len
     * @return java.lang.String
     */
    public static String substringByChar(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len <= 0) {
            return "";
        }
        if (len >= str.length()) {
            return str;
        }
        int count = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FA5) { // 中文字符
                count += 2;
            } else {
                count++;
            }
            if (count > len) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 移除原始字符串两端的空白字符
     * @param str
     * @return
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 首字母转小写
     * @param s
     * @return
     */
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }


    /**
     * 将驼峰式命名转换为下划线格式
     * @param camelCaseStr
     * @return
     */
    public static String toUnderScoreCase(String camelCaseStr) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < camelCaseStr.length(); i++) {
            char c = camelCaseStr.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append("_");
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // 将下划线格式转换为大驼峰式命名 user_name => UserName
    public static String toPascalCase(String underScoreStr) {
        StringBuffer result = new StringBuffer();
        String[] parts = underScoreStr.split("_");
        for (String part : parts) {
            if (part.length() > 0) {
                result.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }


    /**
     *  将下划线格式转换为小驼峰式命名 user_name => userName
     * @param underScoreStr
     * @return
     */
    public static String toCamelCase(String underScoreStr) {
        StringBuffer result = new StringBuffer();
        String[] parts = underScoreStr.split("_");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                result.append(part.toLowerCase());
            } else {
                result.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }

    /**
     * sql排序
     * @param json
     * @param clazz
     * @param suffix
     * @return
     */
    @Deprecated
    public static String toSort(String json, Class<?> clazz, String suffix) {
        StringBuffer sb = new StringBuffer();
        sb.append("ORDER BY");
        StringJoiner sj = new StringJoiner(",");
        Map<String, String> map = null;
        try {
            map = ConvertUtils.cloneDeep(json, Map.class);
        } catch (Exception e) {
            return "";
        }
        if (StrUtils.isEmpty(map)) {
            return "";
        }

        List<Field> fields = BeanUtils.getFields(clazz);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            for (Field field : fields) {
                if (Objects.equals(entry.getKey(), field.getName())) {
                    String key = toUnderScoreCase(field.getName());
                    if ("asc".equals(entry.getValue())) {
                        sj.add(String.format(" %s ASC", key));
                    } else if ("desc".equals(entry.getValue())) {
                        sj.add(String.format(" %s DESC", key));
                    }
                }
            }
        }
        if (StrUtils.isEmpty(sj.toString())) {
            return "";
        }

        if (StrUtils.isNotEmpty(suffix)) {
            String[] strArr = suffix.split(" ");
            if (StrUtils.isNotEmpty(strArr)) {
                for (Field field : fields) {
                    if (Objects.equals(field.getName(), strArr[0])) {
                        String key = toUnderScoreCase(field.getName());
                        if (strArr.length > 1 && strArr[1] != null) {
                            if ("DESC".equals(strArr[1].toUpperCase())) {
                                key += " DESC";
                            }
                        }
                        sj.add(String.format(" %s", key));
                    }
                }
            }

        }
        sb.append(sj);
        return sb.toString();
    }

    @Deprecated
    public static String toSort(String json, Class<?> clazz) {
        return toSort(json, clazz, null);
    }

    public static Matcher matcher(String v, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(v);
    }

    public static boolean contains(String parent, String child) {
        if (parent == null) {
            if (child == null) {
                return true;
            }
            return false;
        }
        if (child == null) {
            return false;
        }
        return parent.contains(child);
    }

    /**
     * arr是否小于两个长度，否则默认补充空字符串
     * @param arr
     * @return
     */
    @Deprecated
    public static String[] toTwo(String[] arr) {
        if (StrUtils.isEmpty(arr)) {
            return new String[]{"", ""};
        }
        if (arr.length < 2) {
            return new String[]{arr[0], ""};
        }
        return arr;
    }

    /**
     * 日期初始化处理: arr是否小于两个长度，否则默认补充为2
     * @param arr
     * @return
     */
    @Deprecated
    public static LocalDate[] toTwo(LocalDate[] arr) {
        if (StrUtils.isEmpty(arr)) {
            return new LocalDate[]{null, null};
        }
        if (arr.length < 2) {
            return new LocalDate[]{arr[0], null};
        }
        return arr;
    }

    /**
     * 时间初始化处理: arr是否小于两个长度，否则默认补充为2
     * @param arr
     * @return
     */
    @Deprecated
    public static LocalDateTime[] toTwo(LocalDateTime[] arr) {
        if (StrUtils.isEmpty(arr)) {
            return new LocalDateTime[]{null, null};
        }
        if (arr.length < 2) {
            return new LocalDateTime[]{arr[0], null};
        }
        return arr;
    }

    /*
     * 生成[min, max]之间的随机整数
     * @param min 最小整数
     * @param max 最大整数
     */
    public static int randomInt(int min, int max){
        return new Random().nextInt(max - min + 1) + min;
    }

    /*
     * 生成[0, max]之间的随机整数
     * @param max 最大整数
     */
    public static int randomInt(int max){
        return randomInt(0, max);
    }


    /**
     * 分组合计
     * @param list
     * @param fn
     * @return
     * @param <T>
     * @param <U>
     */
    public static <T, U> List<T> groupBy(List<T> list, cn.undraw.util.bean.SFunction<T, ?>... fn) {
        return groupBy(list, null, fn);
    }

    /**
     * 分组合计
     * @param list
     * @param con
     * @param fn
     * @return
     * @param <T>
     * @param <U>
     */
    public static <T, U> List<T> groupBy(List<T> list, Consumer<T> con, cn.undraw.util.bean.SFunction<T, ?>... fn) {
        String fieldName = BeanUtils.getFieldName(fn);
        return groupBy(list, con, fieldName);
    }

    /**
     * 分组合计
     * @param list
     * @return
     * @param <T>
     * @param <U>
     */
    public static <T, U> List<T> groupBy(List<T> list, String fieldName) {
        return groupBy(list, null, fieldName);
    }

    /**
     * 分组合计
     * @param list
     * @param con
     * @param fieldName
     * @return
     * @param <T>
     * @param <U>
     */
    public static <T, U> List<T> groupBy(List<T> list, Consumer<T> con, String fieldName) {
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        Object o1 = list.get(0);
        List<Field> fields = BeanUtils.getFields(o1.getClass());
        Map<String, List<T>> map = new LinkedHashMap<>();

        for (T o : list) {
            T o2 = (T) BeanUtils.copy(o);
            if (con != null) {
                con.accept(o2);
            }
            StringBuffer sb = new StringBuffer();

            for (Field field : fields) {
                if (fieldName.contains(field.getName())) {
                    Object fieldValue = BeanUtils.getFieldValue(o2, field.getName());
                    sb.append(fieldValue).append("-");
                }
            }

            sb.delete(sb.length() - 1, sb.length());
            String key = sb.toString();
            List<T> list2 = map.get(key);
            if (list2 == null) {
                map.put(key, new ArrayList<>(Arrays.asList(o2)));
            } else {
                list2.add(o2);
            }
        }

        List newList = new ArrayList();
        for (List<T> value : map.values()) {
            T t = groupByTotal(value, fieldName);
            newList.add(t);
        }

        return newList;
    }

    /**
     *
     * @param list
     * @return
     * @param <T>
     * @param <U>
     */
    public static <T, U> T groupByTotal(List<T> list) {
        return groupByTotal(list, "");
    }

    /**
     * 分组合计
     * @param list
     * @param fn
     * @return
     * @param <T>
     * @param <U>
     */
    public static <T, U> T groupByTotal(List<T> list, SFunction<T, ?>... fn) {
        String fieldName = StrUtils.isNull(BeanUtils.getFieldName(fn), "");
        return groupByTotal(list, fieldName);
    }

    /**
     * 分组合计
     * @param list
     * @param fieldName => fieldName1,fieldName2,fieldName3
     * @return
     * @param <T>
     * @param <U>
     */
    public static <T, U> T groupByTotal(List<T> list, String fieldName) {
        if (list == null || list.size() == 0) {
            return null;
        }

        Object o1 = list.get(0);
        List<Field> fields = BeanUtils.getFields(o1.getClass());
        if (o1 == null) {
            return null;
        }
        T obj = null;
        obj = (T) BeanUtils.copy(o1);

        for (int i = 1; i < list.size(); i++) {
            T object = list.get(i);
            for (Field field : fields) {
                if (Modifier.isFinal(field.getModifiers()) || fieldName.contains(field.getName())) {
                    continue;
                }
                Object v1 = BeanUtils.getFieldValue(obj, field.getName());
                Object v2 = BeanUtils.getFieldValue(object, field.getName());
                if (field.getType() == Double.class || field.getType() == double.class) {
                    Double add = (Double) StrUtils.isNull(v1, 0.0) + (Double) StrUtils.isNull(v2, 0.0);
                    BeanUtils.setFieldValue(obj, field.getName(), add);
                }
                if (field.getType() == Integer.class || field.getType() == int.class) {
                    Integer add = (Integer) StrUtils.isNull(v1, 0) + (Integer) StrUtils.isNull(v2, 0);
                    BeanUtils.setFieldValue(obj, field.getName(), add);
                }
                if (field.getType() == Long.class || field.getType() == long.class) {
                    Long add = (Long) StrUtils.isNull(v1, 0L) + (Long) StrUtils.isNull(v2, 0L);
                    BeanUtils.setFieldValue(obj, field.getName(), add);
                }
                if (field.getType() == String.class) {
                    String add = (String) v1;
                    if (StrUtils.isEmpty(add)) {
                        add = (String) v2;
                    } else {
                        if (StrUtils.isNotEmpty(v2) && !add.contains((String) v2)) {
                            add = add + "," + (String) v2;
                        }
                    }
                    BeanUtils.setFieldValue(obj, field.getName(), add);
                }
            }
        }
        return obj;
    }

    /**
     * 比较
     * @param newList 新数据
     * @param srcList 源数据
     * @param fun
     * @return
     * @param <T>
     */
    public static <T> Compare<T> compare(List<T> newList, List<T> srcList, BiFunction<T, T, Boolean> fun) {
        Compare<T> compare = new Compare<>();
        if (StrUtils.isEmpty(newList)) {
            return compare;
        }
        if (StrUtils.isEmpty(srcList)) {
            compare.saveList.addAll(newList);
            return compare;
        }
        for (T new1 : newList) {
            boolean flag = true;
            for (T src : srcList) {
                Boolean b = fun.apply(new1, src);
                if (b) {
                    compare.updateList.add(new1);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                compare.saveList.add(new1);
            }
        }

        for (T src : srcList) {
            boolean f = true;
            for (T new1 : newList) {
                Boolean b = fun.apply(src, new1);
                if (b) {
                    f = false;
                    break;
                }
            }
            if (f) {
                compare.removeList.add(src);
            }
        }

        return compare;
    }

}

