package com.undraw;

import cn.undraw.util.ConvertUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author readpage
 * @date 2023-02-09 16:04
 */
public class ConvertTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Animal {
        private String name;
        private String eat;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Cat extends Animal {
        private String action;
    }

    private static List<Animal> animalList = new ArrayList<>(Arrays.asList(
            new Animal("猫", "鱼"),
            new Animal("狗", "狗头")
    ));

    @Test
    public void test() {
        System.out.println(ConvertUtils.cloneDeep(new Animal("猫", "鱼"), Cat.class));
        List<Cat> catList = (List<Cat>) ConvertUtils.copy(animalList, Cat.class);
        System.out.println(catList);
        System.out.println("-------------------");
        Animal animal = new Animal("猫", "鱼");
        Cat cat = ConvertUtils.copy(animal, Cat.class);
        cat.setEat("猫粮");
        System.out.println(animal);
        System.out.println(cat);
    }

    @Test
    public void test2() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            numbers.add(i);
        }

        int batchSize = 10; // 每个批次的大小
        List<List<Integer>> batches = ConvertUtils.batchList( numbers, batchSize);

        // 输出每个批次
        for (List<Integer> batch : batches) {
            System.out.println(batch);
        }
    }

    @Test
    public void test3() {

    }
}
