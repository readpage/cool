package com.undraw;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DecimalUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.URLUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author readpage
 * @date 2023-03-03 11:20
 */
public class StrTest {
    @Test
    public void test() {
        System.out.println(ConvertUtils.toJson((Object) null));
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, null, 3));
        HashSet hashSet = new HashSet();
        hashSet.add(2);
        hashSet.add(5);
        hashSet.add(3);
        System.out.println(StrUtils.join(list, ","));
        System.out.println(StrUtils.join(hashSet, ","));
        System.out.println(StrUtils.join(new String[]{"2", "4", "8"}, "|"));
        String o = "1";
        BigDecimal bigDecimal = null;
        System.out.println(bigDecimal);

        Double a = new Double(2);
        double b = a;
        System.out.println(b);
        System.out.println(DecimalUtils.toCurrency(null));
    }

    @Test
    public void test2() {
        String j = String.valueOf("9.318443295E7");
        String b = String.valueOf("5.959610484E7");
        System.out.println(DecimalUtils.sub(j, b));
        double div = DecimalUtils.div(DecimalUtils.sub(j, b), ConvertUtils.toDouble("59596104.840000"));
        System.out.println(div);
        System.out.println(DecimalUtils.toPercent(div));
        double a = 3.45400312412;
        Double k = 3.454001314;
        String c = "3.454003124120000";
        System.out.println((Double)a);
        System.out.println((double) k);
        System.out.println(Double.valueOf("3.454003124120000"));
        System.out.println(new BigDecimal(c).doubleValue());
    }

    @Test
    public void test3() {
        String j = String.valueOf("9.318443295E7");
        String b = String.valueOf("5.959610484E7");
        System.out.println(DecimalUtils.sub(j, b));
        double div = DecimalUtils.div(DecimalUtils.sub(j, b), ConvertUtils.toDouble("59596104.840000"));
        System.out.println(div);
        System.out.println(DecimalUtils.toPercent(div));
        System.out.println("**************");
        BigDecimal subtract = new BigDecimal("93184432.950000").subtract(new BigDecimal("59596104.840000"));
        System.out.println(subtract);
        System.out.println(subtract.divide(new BigDecimal("59596104.840000"), 10, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    public void removePrefix() {
        String a = "$123";
        String b = "hello world!";
        String c = "hello.txt";
        System.out.println(StrUtils.removePrefix(a, "$"));
        System.out.println(StrUtils.removePrefix(b, "hello "));
        System.out.println(StrUtils.removeSuffix(c, ".txt"));
        String msg = "/driver-trip-record/2023/03-23/捕获3.PNG";
        System.out.println(URLUtils.encode(msg));
    }

    @Test
    public void convert() {
        String string = ConvertUtils.toString(null);
        System.out.println(string);
        System.out.println(string.isEmpty());
    }

    @Test
    public void test5() {
        String format = String.format("%d hello world %s", 1L, 2);
        System.out.println(format);
    }
}
