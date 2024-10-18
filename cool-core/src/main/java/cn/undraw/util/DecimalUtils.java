package cn.undraw.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 * Double计算精度丢失（金融计算）
 * 简化BigDecimal计算的小工具类
 * @author readpage
 * @date 2023-03-13 19:38
 */
public class DecimalUtils {
    /**
     * 默认除法运算精度
     */
    private static final int DEF_DIV_SCALE = 10;

    /**
     * 提供精确的加法运算
     *
     * @param v1 被加数
     * @param v2 加数
     * @return double 两个参数的和
     */
    public static double add(String v1, String v2) {
        return add(ConvertUtils.toDouble(v1), ConvertUtils.toDouble(v2));
    }

    /***
     * 提供精确的加法运算。
     * @param v1 被加数
     * @param v1 加数
     * @return double 两个参数的和
     */
    public static double add(Double v1, Double v2) {
        v1 = StrUtils.isNull(v1, 0.0);
        v2 = StrUtils.isNull(v2, 0.0);
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.add(b2).doubleValue();
    }


    /**
     * 提供精确的减法运算。
     *
     * @param v1 被减数
     * @param v2 减数
     * @return double 两个参数的差
     */
    public static double sub(String v1, String v2) {
        return sub(ConvertUtils.toDouble(v1), ConvertUtils.toDouble(v2));
    }

    /**
     * 提供精确的减法运算。
     * @param v1 被减数
     * @param v2 减数
     * @return double 两个参数的差
     */
    public static double sub(Double v1, Double v2) {
        v1 = StrUtils.isNull(v1, 0.0);
        v2 = StrUtils.isNull(v2, 0.0);
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return BigDecimal 两个参数的积
     */
    public static double mul(String v1, String v2) {
        return mul(ConvertUtils.toDouble(v1), ConvertUtils.toDouble(v2));
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return double 两个参数的积
     */
    public static double mul(Double v1, Double v2) {
        v1 = StrUtils.isNull(v1, 0.0);
        v2 = StrUtils.isNull(v2, 0.0);
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.multiply(b2).doubleValue();
    }


    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1 被除数
     * @param v2 除数
     * @return double 两个参数的商
     */
    public static double div(String v1, String v2) {
        return div(ConvertUtils.toDouble(v1), ConvertUtils.toDouble(v2));
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1 被除数
     * @param v2 除数
     * @return double 两个参数的商
     */
    public static double div(Double v1, Double v2) {
        v1 = StrUtils.isNull(v1, 0.0);
        v2 = StrUtils.isNull(v2, 0.0);
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * 取余数
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 小数点后保留几位
     * @return double 余数
     */
    public static double div(String v1, String v2, int scale) {
        return div(ConvertUtils.toDouble(v1), ConvertUtils.toDouble(v2), scale);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return double 两个参数的商
     */
    public static double div(Double v1, Double v2, int scale) {
        v1 = StrUtils.isNull(v1, 0.0);
        v2 = StrUtils.isNull(v2, 0.0);
        if (v2 == 0) return 0;
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 取余数  BigDecimal
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 小数点后保留几位
     * @return 余数
     */
    public static BigDecimal div(BigDecimal v1, BigDecimal v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        if (isZero(v2)) return new BigDecimal("0");
        return v1.divide(v2).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 提供精确的小数位四舍五入处理。保留两位
     *
     * @param v     需要四舍五入的数字
     * @return String 四舍五入后的结果
     */
    public static String round(String v) {
        return round(v, 2);
    }

    /**
     * 提供精确的小数位四舍五入处理。保留两位
     *
     * @param v     需要四舍五入的数字
     * @return String 四舍五入后的结果
     */
    public static String round(Double v) {
        return round(v, 2);
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return String 四舍五入后的结果
     */
    public static String round(String v, int scale) {
        Double a = ConvertUtils.toDouble(v);
        if (a == null) return "0";
        return round(a, scale);
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return String 四舍五入后的结果
     */
    public static String round(Double v, int scale) {
        if (v == null) return "0";
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = BigDecimal.valueOf(v);
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 提供精确的小数位四舍五入处理。保留两位
     *
     * @param v     需要四舍五入的数字
     * @return String 四舍五入后的结果
     */
    public static String floor(Double v) {
        return floor(v, 2);
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return String 四舍五入后的结果
     */
    public static String floor(String v, int scale) {
        Double a = ConvertUtils.toDouble(v);
        if (a == null) return "0";
        return floor(a, scale);
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return String 四舍五入后的结果
     */
    public static String floor(Double v, int scale) {
        if (v == null) return "0";
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = BigDecimal.valueOf(v);
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_DOWN).toString();
    }

    /**
     * 提供精确的类型转换(Float)
     *
     * @param v 需要被转换的数字
     * @return float 返回转换结果
     */
    public static float toFloat(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.floatValue();
    }

    /**
     * 提供精确的类型转换(Int)不进行四舍五入
     *
     * @param v 需要被转换的数字
     * @return int 返回转换结果
     */
    public static int toInt(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.intValue();
    }

    /**
     * 提供精确的类型转换(Long)
     *
     * @param v 需要被转换的数字
     * @return long 返回转换结果
     */
    public static long toLong(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.longValue();
    }

    /**
     * 返回两个数中大的一个值
     *
     * @param v1 需要被对比的第一个数
     * @param v2 需要被对比的第二个数
     * @return double 返回两个数中大的一个值
     */
    public static double max(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.max(b2).doubleValue();
    }

    /**
     * 返回两个数中小的一个值
     *
     * @param v1 需要被对比的第一个数
     * @param v2 需要被对比的第二个数
     * @return double 返回两个数中小的一个值
     */
    public static double min(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.min(b2).doubleValue();
    }

    /**
     * 精确对比两个数字
     *
     * @param v1 需要被对比的第一个数
     * @param v2 需要被对比的第二个数
     * @return int 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
     */
    public static int compare(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.compareTo(b2);
    }

    /**
     * 建立百分比格式化引用,小数点后保留两位
     * @param obj
     * @return java.lang.String 返回百分数,小数点后保留两位
     */
    public static String toPercent(Object obj) {
        return toPercent(obj, 2);
    }

    /**
     * 建立百分比格式化引用
     * @param obj
     * @param scale 小数点后保留几位
     * @return 返回百分数,保留到第n位
     */
    public static String toPercent(Object obj, int scale) {
        Double a = ConvertUtils.toDouble(obj);
        if (a == null) return "0%";
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(scale);
        return percent.format(a);
    }

    /**
     * 建立货币格式化
     * @param obj
     * @return java.lang.String 返回货币格式
     */
    public static String toCurrency(Object obj) {
        Double a = ConvertUtils.toDouble(obj);
        if (a == null) return "0";
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        return currency.format(a).substring(1);
    }

    /**
     * 判断BigDecimal是否等于0
     * @param val
     * @return boolean
     */
    public static boolean isZero(BigDecimal val) {
        return val.compareTo(new BigDecimal("0.0")) == 0;
    }

    /**
     * 判断BigDecimal是否不等于0
     * @param val
     * @return boolean
     */
    public static boolean isNotZero(BigDecimal val) {
        return val.compareTo(new BigDecimal("0.0")) != 0;
    }

    public static String toString(Object o) {
        if (!StrUtils.isNumber(o)) {
            return null;
        }
        return new BigDecimal(String.valueOf(o)).toString();
    }
}
