package com.undraw.core;

import cn.undraw.util.ConvertUtils;
import com.undraw.domain.model.Employee;
import com.undraw.domain.model.Fruit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author readpage
 * @date 2023-03-04 13:03
 */
@SpringBootTest
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

    @Test
    public void format() {
        Fruit fruit = new Fruit();
        BigDecimal decimal = new BigDecimal("2.2225");
        fruit.setPrice(decimal);
        fruit.setScore(380535617.050000);
        fruit.setPrice2(new BigDecimal(480535617.050000));
        fruit.setPrice3(new BigDecimal(580535617.050000));
        System.out.println("序列化前--------");
        System.out.println(fruit);
        System.out.println("序列化后----------");
        String json = ConvertUtils.toJson(fruit);
        System.out.println(json);
    }

    @Test
    public void test1() {
        List<Employee> employeeList = Employee.employeeList;
        List<Employee> newList = ConvertUtils.cloneDeep(Employee.employeeList.subList(0, 3));
        newList.get(0).setId(11L);
        List<Employee> list = employeeList.stream().filter(o -> newList.contains(o)).collect(Collectors.toList());
        System.out.println(list);
    }

    @Test
    public void test2() {
        ConvertUtils convertUtils = new ConvertUtils();
        Employee employee = Employee.employeeList.get(0);
        employee.setCreateTime(LocalDateTime.now());
        Employee employee1 = convertUtils.cloneDeep(employee, Employee.class);
        System.out.println(employee1);

    }


}
