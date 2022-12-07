package cn.undraw.util;

import org.omg.CORBA.Object;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author readpage
 * @date 2022-11-10 9:35
 */
public class StrUtils {


    /**
     * 判断是否是数字
     * @param val
     * @return
     * @param <T>
     */
    public static<T> boolean isNumber(T val) {
        try {
            String s = String.valueOf(val);
            return s.matches("([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])");
        } catch (Exception e){
            return false;
        }
    }

    /**
     * 判断是否为空
     * @param obj
     * @return
     * @param <T>
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
            if (obj instanceof int[]) {
                return ((int[]) obj).length == 0;
            } else if (obj instanceof Integer[]) {
                return ((Integer[]) obj).length == 0;
            } else if (obj instanceof double[]) {
                return ((double[]) obj).length == 0;
            } else if (obj instanceof long[]) {
                return ((long[]) obj).length == 0;
            } else if (obj instanceof Long[]) {
                return ((Long[]) obj).length == 0;
            }
        }
        return false;
    }

    /**
     * 判断是否不能为空
     * @param obj
     * @return
     * @param <T>
     */
    public static<T> boolean isNotEmpty(T obj) {
        return !isEmpty(obj);
    }

    /**
     * val不为空空返回val值,否则返回val2值
     * @param val
     * @param val2
     * @return
     * @param <T>
     */
    public static<T> T isNull(T val, T val2) {
        return isNotEmpty(val) ? val : val2;
    }

    /**
     * 两个参数比较，如果返回结果等于1，则返回第二个参数，否则返回第一个参数
     * @param val
     * @param val2
     * @param biFunction
     * @return
     * @param <T>
     */
    public static<T> T isExceed(T val, T val2, BiFunction<T, T, Integer> biFunction) {
        return biFunction.apply(val, val2) == 1 ? val2: val;
    }

    /**
     * 两个参数比较，如果返回结果等于1，则返回第二个参数，否则返回第一个参数
     * @param val
     * @param val2
     * @return
     */
    public static Integer isExceed(Integer val, Integer val2) {
        return Integer.compare(val, val2) == 1 ? val2: val;
    }


    /**
     * 四舍五入并保留固定小数位
     * @param val
     * @param n 保留到第几位
     * @return
     */
    public static Double toFixed(Double val, int n) {
        String str = "%." +n + "f";
        return Double.parseDouble(String.format(str, isNull(val, 0.0)));
    }

    /**
     * 四舍五入并保留到第2位
     * @param val
     * @return
     */
    public static Double toFixed(Double val) {
        return toFixed(val);
    }

    /**
     * 四舍五入并保留固定小数位
     * @param val
     * @param n 保留到第几位
     * @return
     */
    public static Float toFixed(Float val, int n) {
        String str = "%." +n + "f";
        return Float.parseFloat(String.format(str, isNull(val, 0.0)));
    }

    /**
     * 四舍五入并保留到第2位
     * @param val
     * @return
     */
    public static Float toFixed(Float val) {
        return toFixed(val);
    }

    /**
     * 判断BigDecimal是否等于0
     */
    public static boolean isZero(BigDecimal val) {
        return val.compareTo(new BigDecimal("0.0")) == 0;
    }

    /**
     * 判断BigDecimal是否不等于0
     */
    public static boolean isNotZero(BigDecimal val) {
        return val.compareTo(new BigDecimal("0.0")) != 0;
    }
}
