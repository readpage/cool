package com.undraw;

import cn.undraw.util.ConvertUtils;
import com.undraw.model.Fruit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author readpage
 * @date 2023-03-04 13:03
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ConvertTest2 {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Hello {
        List<LocalDate> localDate;
    }

    @Test
    public void test() {
        // TimeFormatConfig转换器
        System.out.println(ConvertUtils.toObject("{\"localDate\": [\"2023-01-02\", \"2023-01-03\"]}", Hello.class));
    }

    @Resource
    private Fruit fruit;
    @Test
    public void format() {
        Fruit fruit = new Fruit();
        fruit.getPrice();
        BigDecimal decimal = new BigDecimal("2.2225");
//        fruit.setPrice(decimal);
//        System.out.println("序列化前--------");
//        System.out.println(decimal);
//        System.out.println("序列化后----------");
//        Fruit newFruit = ConvertUtils.cloneDeep(fruit, Fruit.class);
//        System.out.println(newFruit);
    }

}
