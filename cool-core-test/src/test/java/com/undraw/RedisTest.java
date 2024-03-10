package com.undraw;

import com.undraw.util.redis.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @author readpage
 * @date 2023-12-11 13:05
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {
    // https://blog.csdn.net/qq_17858343/article/details/130618975
    @Resource
    private RedisUtil redisUtil;

    @Test
    public void test() {
        redisUtil.sRemove("test", new ArrayList<>().toArray());
    }
}
