package cn.undraw.util;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Map;

/**
 * 避免计算精度丢失（金融计算）
 * 简化BigDecimal计算的小工具类
 * @author readpage
 * @date 2023-03-13 19:38
 */
public class DecimalUtils {

    /**
     * spel表达式
     * @param expression
     * @param vars
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T spel(String expression, Map<String, Object> vars, Class<T> clazz) {
        SpelExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        vars.forEach(context::setVariable);
        T v = parser.parseExpression(expression).getValue(context, clazz);
        return v;
    }

    /**
     * spel calc 数字计算
     * @param expression
     * @param vars
     * @return
     */
    public static BigDecimal calc(String expression, Map<String, Object> vars) {
        SpelExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        vars.forEach((k, v) -> context.setVariable(k,
                StrUtils.isNumber(v) ? new BigDecimal(v.toString()) : v));
        return parser.parseExpression(expression).getValue(context, BigDecimal.class);
    }

    /**
     * 提供精确的小数位处理。
     *
     * @param v     需要处理的数字
     * @param scale 小数点后保留几位
     * @return String 处理后的结果
     */
    public static String round(Double v, int scale, RoundingMode roundingMode) {
        if (v == null) return "0";
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal value = BigDecimal.valueOf(v);
        return value.setScale(scale, roundingMode).toString();
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
        return round(v, scale, RoundingMode.HALF_UP).toString();
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
     * 提供精确的小数位舍入模式向下舍入。保留两位
     *
     * @param v     需要向下舍入的数字
     * @return String 向下舍入后的结果
     */
    public static String down(Double v) {
        return down(v, 2);
    }

    /**
     * 提供精确的小数位舍入模式向下舍入。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return String 四舍五入后的结果
     */
    public static String down(String v, int scale) {
        Double a = ConvertUtils.toDouble(v);
        return down(a, scale);
    }

    /**
     * 提供精确的小数位舍入模式向下舍入。
     *
     * @param v     需要向下舍入的数字
     * @param scale 小数点后保留几位
     * @return String 向下舍入后的结果
     */
    public static String down(Double v, int scale) {
        return round(v, scale, RoundingMode.DOWN).toString();
    }

    /**
     * 提供精确的小数位舍入模式向上舍入。保留两位
     *
     * @param v     需要向上舍入的数字
     * @return String 向上舍入后的结果
     */
    public static String up(Double v) {
        return up(v, 2);
    }

    /**
     * 提供精确的小数位舍入模式向上舍入。
     *
     * @param v     需要向上舍入的数字
     * @param scale 小数点后保留几位
     * @return String 向上舍入后的结果
     */
    public static String up(String v, int scale) {
        Double a = ConvertUtils.toDouble(v);
        return up(a, scale);
    }

    /**
     * 提供精确的小数位舍入模式向上舍入。
     *
     * @param v     需要向上舍入的数字
     * @param scale 小数点后保留几位
     * @return String 向上舍入后的结果
     */
    public static String up(Double v, int scale) {
        return round(v, scale, RoundingMode.UP).toString();
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

    public static String toString(Object o) {
        if (!StrUtils.isNumber(o)) {
            return null;
        }
        return new BigDecimal(String.valueOf(o)).toString();
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

}
