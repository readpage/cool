package com.undraw;

import cn.undraw.util.snowflake.Snowflake;
import cn.undraw.util.snowflake.SnowflakeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author readpage
 * @date 2023-07-05 13:57
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SnowflakeTest {

    @Test
    public void test() {
        Snowflake idWorker = new Snowflake(0, 0);
        for (int i = 0; i < 1000; i++) {
            long id = idWorker.nextId();
            System.out.println(id);
        }
    }

    @Test
    public void test2() {
        for (int i = 0; i < 1000; i++) {
            long id = SnowflakeUtils.nextId();
            System.out.println(id);
        }
    }
}
