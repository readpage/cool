package com.undraw.basic;

import cn.undraw.util.DecimalUtils;
import org.junit.jupiter.api.Test;

/**
 * @author readpage
 * @date 2023-03-13 19:45
 */
public class DecimalTest {
    @Test
    public void test() {
        Double a = Double.valueOf(1);
        System.out.println(a);
        double b = a;
        System.out.println(b);
        System.out.println(DecimalUtils.toCurrency("1000000"));
        System.out.println(DecimalUtils.down(0.235));
    }

    @Test
    public void test2() {
        Double round = 480535617.050000;
        System.out.println(round);
        System.out.println(DecimalUtils.toString(round));
    }

    @Test
    public void test3() {
        System.out.println(DecimalUtils.toPercent("0.568684"));
    }

    @Test
    public void test4() {
        Double a = 0.010;
        Double b = 0.010;
        Double c = 0.010;
        Double s = a + b + c;
        System.out.println(s);
    }
}
