package com.undraw;

import cn.undraw.util.bean.AnnoUtils;
import cn.undraw.util.bean.BeanUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.undraw.domain.entity.User;
import com.undraw.domain.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author readpage
 * @date 2025-02-15 11:07
 */
public class BeanTest {

    @Test
    public void test() {
        System.out.println(BeanUtils.getFieldName(User::getUsername, User::getPassword, User::getPhone));
        Employee employee = Employee.employeeList.get(0);
        String fieldName = null;
        for (Field field : BeanUtils.getFields(Employee.class)) {
            Object access = AnnoUtils.getValue(field, JsonProperty.class, "access");
            if (Objects.equals("READ_ONLY", String.valueOf(access))) {
                fieldName = field.getName();
            }
        }
        Object fieldValue = BeanUtils.getFieldValue(employee, fieldName);
        System.out.println(fieldValue);
        BeanUtils.setFieldValue(employee, fieldName, LocalDateTime.now().minusDays(1));
        System.out.println(employee.getCreateTime());
    }

    @Test
    public void test1() {
        BeanUtils.getFields(User.class).forEach(field -> {
            Object access = AnnoUtils.getValue(field, JsonProperty.class, "access");
            System.out.println(access);
        });
        System.out.println("------------------");
        Object title = AnnoUtils.getValue(User.class, Schema.class, "title");
        System.out.println(title);
    }

    @Test
    public void test2() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("password");
        User user2 = new User();
        user2.setUsername("user2");
        user2.setAge(20);
        BeanUtils.merge(user, user2);
        System.out.println("user1: " + user);
        System.out.println("user2: " + user2);
    }

    @Test
    public void equals() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("password2");
        user.setAge(20);
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setAge(20);
        User user3 = new User();
        user3.setUsername("user2");
        user3.setPassword("password2");
        user3.setAge(30);
        System.out.println(BeanUtils.getFieldName(User::getUsername, User::getPassword, User::getPhone));
        boolean b1 = BeanUtils.equals(user, user2, User::getUsername, User::getPassword);
        System.out.println(b1);
        boolean b2 = BeanUtils.equals(user2, user3, User::getUsername, User::getPassword);
        System.out.println(b2);
        boolean b3 = BeanUtils.equals(user, user2, User::getPassword, User::getAge);
        System.out.println(b3);
        boolean b4 = BeanUtils.equals(user2, user3, User::getPassword, User::getAge);
        System.out.println(b4);
    }

    @Test
    public void copy() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("password");
        User copy = BeanUtils.copy(user);
        copy.setAge(20);
        copy.setPassword("password2");
        System.out.println(copy);
        System.out.println(user);
    }

}
