package com.undraw.other;

import cn.undraw.util.ThrottleUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ThrottleTest {

    @Resource
    private ThrottleUtil throttleUtil;

    @Test
    public void action() {
        for (int i = 0; i < 100; i++) { // 模拟快速连续调用
            throttleUtil.action(() -> System.out.println("Executing action at " + System.currentTimeMillis()));
            try {
                Thread.sleep(50); // 模拟快速连续调用
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
