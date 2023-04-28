package com.undraw;

import cn.undraw.util.StrUtils;
import org.junit.Test;

/**
 * @author readpage
 * @date 2022-12-07 20:13
 */
public class DemoTest {
    @Test
    public void test() {
        String str = null;
        System.out.println(StrUtils.isEmpty(str));
    }

    @Test
    public void test2() {
        System.out.println(StrUtils.randomName());
    }

}
