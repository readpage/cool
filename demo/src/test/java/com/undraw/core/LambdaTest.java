package com.undraw.core;

import cn.undraw.util.StrUtils;
import com.undraw.domain.model.Employee;
import com.undraw.domain.model.Model;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *  java内置的4大核心函数接口
 *
 *  消费型接口 Consumer<T>        void accept(T t)
 *  供给型接口 Supplier<T>        T get()
 *  函数型即可 Function<T, R>     R apply(T t)
 *  断定型接口 Predicate<T>       boolean test(T t)
 */
public class LambdaTest {

    public void consumerTest(Consumer<Model> consumer) {
        Model model = new Model();
        consumer.accept(model);
        System.out.println(model);
    }

    public String function(Function<Model, Model> fun) {
        Model model = new Model();
        Model r = fun.apply(model);
        return r.getLabel();
    }

    @Test
    public void consumer() {
        consumerTest(o -> o.setLabel("key"));
    }

    @Test
    public void function() {
        String fun = function(o -> {
            o.setLabel("key");
            return o;
        });
        System.out.println(fun);
    }

    public <T> T supplier(Supplier<T> sup) {
        return sup.get();
    }

    @Test
    public void supplier() {
        String s = "hello world!";
        String str = supplier(() -> s);
        System.out.println(str);
    }

    private static List<Employee> employeeList = Employee.employeeList;

    @Test
    public void groupingBy() {
        Map<String, List<Employee>> collect = employeeList.stream().collect(Collectors.groupingBy(o -> o.getSex()));
        System.out.println(collect);
    }

    @Test
    public void group() {
        List<Employee> employees = StrUtils.groupBy(employeeList, o -> {
            o.setId(0L);
        }, Employee::getSex, Employee::getAge);
        System.out.println(employees);
    }

    @Test
    public void group2() {
        Employee employee = StrUtils.groupByTotal(employeeList);
        System.out.println(employee);
    }

    @Test
    public void sort() {
        List<Employee> list = employeeList.stream().sorted((o1, o2) -> {
            if (Objects.equals(o1.getName(), "刘一")) return -1;
            if (Objects.equals(o2.getName(), "刘一")) return 1;
            return Double.compare(o2.getAge(), o1.getAge());
        }).limit(5).collect(Collectors.toList());
        System.out.println(list);
    }
}
