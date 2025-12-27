package com.undraw.core;

import cn.undraw.util.ConvertUtils;
import com.undraw.domain.model.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.*;

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
        Animal animal = ConvertUtils.cloneDeep(new Animal("猫", "鱼"));
        System.out.println(animal);
    }

    @Test
    public void test4() {
        List<Student> list = Student.studentList;
        Set<Student> set = new TreeSet<>(Comparator.comparing(Student::getId));
        set.addAll(list);
        List<Student> list1 = ConvertUtils.cloneDeep(set);
        list.removeIf(o -> o.getId() == 1);
        System.out.println(list);
        System.out.println(list1);
    }

    @Test
    public void test5() {
//        System.out.println(ConvertUtils.toObject("05d597bcf69b8771ca11faf924e6f4ab95799de4", String.class));
        Integer o = ConvertUtils.cloneDeep("2323", Integer.class);
        System.out.println(o);
        String s = ConvertUtils.toJson(1);
        System.out.println(s);
    }

}
