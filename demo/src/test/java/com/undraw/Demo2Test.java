package com.undraw;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author readpage
 * @date 2023-08-03 8:47
 */
@SpringBootTest
@Slf4j
public class Demo2Test {
    @Test
    public void test() {
        for (int i = 0; i < 10000; i++) {
            log.info("hello world!" + " " +i);
        }
    }
}
