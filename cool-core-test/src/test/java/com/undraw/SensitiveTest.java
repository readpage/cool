package com.undraw;

import cn.undraw.util.filter.SensitiveUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author readpage
 * @date 2023-04-03 22:15
 */
public class SensitiveTest {
    private static List<String> list = new ArrayList<>();

    @Test
    public void test() {
        System.out.println(SensitiveUtils.filter("李明你好!"));
        SensitiveUtils.add("李明");
        System.out.println(SensitiveUtils.filter("李明你好!"));
        SensitiveUtils.remove("李明");
        System.out.println(SensitiveUtils.filter("李明你好!"));
    }

    @Test
    public void test2() {
    }
}
