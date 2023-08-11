package com.undraw;

import cn.undraw.util.RestTemplateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Vector;

/**
 * @author readpage
 * @date 2023-08-03 8:47
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Demo2Test {
    @Resource
    private RestTemplateUtil restTemplateUtil;
    @Test
    public void test() {
        Vector<Thread> threadVector = new Vector<>();
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                restTemplateUtil.get("http://192.168.1.34:9106/consumption/manualSync?date=2023-08-01&type=3");
            });
            threadVector.add(thread);
            thread.start();
        }
        for (Thread thread : threadVector) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
