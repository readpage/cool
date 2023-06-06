package cn.undraw.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author readpage
 * @date 2022-11-10 9:35
 */
public class StrUtils {


    /**
     * 判断是否是数字
     * @param val
     * @return boolean
     */
    public static<T> boolean isNumber(T val) {
        try {
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
}
