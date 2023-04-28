package com.undraw;

import cn.undraw.util.DecimalUtils;
import org.junit.Test;

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
}
