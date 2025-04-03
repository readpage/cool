package com.undraw;

import cn.undraw.util.DecimalUtils;
import org.junit.jupiter.api.Test;

/**
 * @author readpage
 * @date 2023-03-13 19:45
 */
public class DecimalTest {
    @Test
    public void test() {
        Double a = new Double(1);
        System.out.println(a);
        double b = a;
        System.out.println(b);
        System.out.println(DecimalUtils.toCurrency("1000000"));
        System.out.println(DecimalUtils.floor(0.235));
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
}
