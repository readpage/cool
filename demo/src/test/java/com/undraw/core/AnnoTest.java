package com.undraw.core;


import cn.idev.excel.annotation.ExcelProperty;
import cn.undraw.util.bean.AnnoUtils;
import cn.undraw.util.bean.BeanUtils;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.undraw.domain.entity.User;
import com.undraw.domain.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.poi.ss.formula.functions.T;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@SpringBootTest
public class AnnoTest {
    @Test
    public void test() {
        List<Employee> list = Employee.employeeList;
        list.forEach(o -> {
            Class<?> clazz = o.getClass();

            // 获取类的所有方法
            Method[] methods = clazz.getMethods();

            for (Method method : methods) {
                // 检查方法名是否以"get"开头，并且没有参数
                if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                    try {
                        // 调用get方法
                        Object value = method.invoke(o);
//                        // 输出方法名和返回值
                        System.out.println("Method: " + method.getName() + ", Value: " + value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Test
    public void test2() {
        List<Employee> list = Employee.employeeList;
        Class<?> clazz = list.get(0).getClass();
        for (Field field : BeanUtils.getFields(clazz)) {
//            String[] annoValue = AnnoUtils.getAnnoValueByField(field, JsonProperty.class, "access");
            Annotation annotation = field.getAnnotation(JsonProperty.class);
            T t = null;
            if (annotation != null) {
                System.out.println(annotation);
//                Map map = ConvertUtils.cloneDeep(annotation.toString(), Map.class);
//                System.out.println(map);
            }
        }
    }

    @Test
    public void getValueByClass() {
        Employee employee = Employee.employeeList.get(0);
        Object valueByClass = AnnoUtils.getValueByClass(employee.getClass(), Schema.class, "title");
        System.out.println(valueByClass);
        Object valueByClass1 = AnnoUtils.getValueByClass(User.class, TableName.class);
        System.out.println(valueByClass1);
    }

    @Test
    public void getValueField() {
        Employee employee = Employee.employeeList.get(0);
        Field field = BeanUtils.getField(employee.getClass(), "crateDate");
        String[] valueByField = (String[])AnnoUtils.getValueByField(field, ExcelProperty.class);
        System.out.println(valueByField[0]);
    }

}
