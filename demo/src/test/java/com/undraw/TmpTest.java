package com.undraw;

import cn.undraw.util.StrUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TmpTest {

    @Test
    public void test() {
        Double a = 0.000;
        System.out.println(a == 0);
    }

    public String test2() {
        String bpcNum = "CCN1100036";
        String category = "CCN11";

        int max = 10;
        if (StrUtils.isEmpty(category)) {
            return "";
        }
        int n = max - category.length();

        if (StrUtils.isEmpty(bpcNum)) {
            return String.format("%s%0" + n + "d", category, 1);
        }

        String num = bpcNum.substring(category.length());
        List<Long> numbers = StrUtils.findNumbers(num);
        return String.format("%s%0" + n + "d", category, numbers.get(0) + 1);
    }
}
