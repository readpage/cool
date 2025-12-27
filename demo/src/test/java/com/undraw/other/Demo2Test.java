package com.undraw.other;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * @author readpage
 * @date 2023-08-03 8:47
 */
@Slf4j
public class Demo2Test {
    @Test
    public void test() {
        BigDecimal bd = new BigDecimal("123.456789");
        String formatted = String.format("%.2f", bd); // 输出 "123.46"
        System.out.println(formatted);
    }
}
