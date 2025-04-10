package com.undraw;

import cn.undraw.util.StrUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        System.out.println(LocalDate.now().getDayOfWeek().getValue());
        System.out.println(LocalDate.now().minusDays(3));
    }

    @Test
    public void test3() {
        LocalDateTime now = LocalDateTime.now();
    }

}
