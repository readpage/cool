package com.undraw;

import cn.undraw.handler.exception.customer.CustomerException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Tmp2Test {

    @Test
    public void test() {
        throw new RuntimeException("错误");
    }

    @Test
    public void test2() {
        throw new CustomerException("警告");
    }
}
