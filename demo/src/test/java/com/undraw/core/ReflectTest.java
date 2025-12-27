package com.undraw.core;

import cn.undraw.util.ReflectUtils;
import cn.undraw.util.StrUtils;
import com.undraw.domain.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ReflectTest {
    @Test
    public void test() {

    }

    @Test
    public void test2() {
        List<Field> fields = ReflectUtils.getFields(Employee.class);
        fields.forEach(System.out::println);
        Field field = fields.get(1);
        System.out.println(field.getName().equals("id"));
    }

    @Test
    public void test3() {
        Employee employee = Employee.employeeList.get(0);
        Map<String, Object> map = ReflectUtils.getEntry(employee);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey()+ " - " + entry.getValue());
        }
    }

    @Test
    public void test4() {
        System.out.println(StrUtils.toCamelCase("user_name"));
        System.out.println(StrUtils.toUnderScoreCase("userName"));
    }
}
