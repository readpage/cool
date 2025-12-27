package com.undraw.basic;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DateUtils;
import com.undraw.domain.model.Student;
import com.undraw.util.redis.RedisUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * @author readpage
 * @date 2023-12-11 13:05
 */
@SpringBootTest
public class RedisTest {
    // https://blog.csdn.net/qq_17858343/article/details/130618975
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void test() {
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.watch("key");
        Integer n = redisUtil.get("key", Integer.class);
        redisUtil.incr("key", 1);
        System.out.println(n);
        redisTemplate.multi();

        // 执行事务
        redisTemplate.exec();
    }

    @Test
    public void test2() {
        redisUtil.set("test", "test");
        boolean b = redisUtil.sHasKey("set", 4);
        System.out.println(b);
    }


    @Test
    public void tets3() {
        redisUtil.zAdd("score", 1, DateUtils.toMilli(LocalDate.now().minusDays(1)));
        redisUtil.zAdd("score", 2, DateUtils.toMilli(LocalDate.now()));
        redisUtil.zAdd("score", 3, DateUtils.toMilli(LocalDate.now().plusDays(1)));
        // 删除时间戳从 0 到当前时间戳的 score 值
        Long n = redisUtil.zRemoveRangeByScore("score", 0, DateUtils.toMilli());
        System.out.println(n);
    }

    @Test
    public void hash() {
        redisUtil.hSet("hash", "2024", Student.studentList.get(0));
        Student student = ConvertUtils.cloneDeep(redisUtil.hGet("hash", "2024"), Student.class);
        System.out.println(student);
    }

    @Test
    public void scan() {
        List<String> list = redisUtil.scan("r*");
        System.out.println(list);
    }
}
