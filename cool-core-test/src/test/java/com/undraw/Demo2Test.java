package com.undraw;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author readpage
 * @date 2023-08-03 8:47
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class Demo2Test {
    @Test
    public void test() {
        for (int i = 0; i < 10000; i++) {
            log.info("hello world!" + " " +i);
        }
    }
}
