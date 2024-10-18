package com.undraw;


import com.undraw.domain.model.Employee;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
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


}
