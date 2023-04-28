package com.undraw;

import cn.undraw.util.RestTemplateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author readpage
 * @date 2023-02-02 17:00
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RestTemplateTest {

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Test
    public void test() {
    }
}
