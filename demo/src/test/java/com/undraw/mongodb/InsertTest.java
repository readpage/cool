package com.undraw.mongodb;

import com.undraw.domain.entity.Person;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class InsertTest {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 插入操作
     * insert表示插入
     * 如果重复插入就报错
     */
    @Test
    public void insert() {
        Person person = Person.builder().name("张三").age(18).id(1L).build();
        mongoTemplate.insert(person);
    }

    /**
     * save表示如果有就修改 如果没有就插入
     */
    @Test
    public void save() {
        Person person = Person.builder().name("张三1").age(18).id(1L).build();
        mongoTemplate.save(person);
    }

    /**
     * 自定义集合
     * 插入文档
     * 相当于将person 这个集合复制到person111里
     */
    @Test
    public void insert2() {
        Person person = Person.builder().name("张三1").age(18).id(1L).build();
        mongoTemplate.insert(person, "person111");
    }

    /**
     * 批量插入
     */
    @Test
    public void insertAll() {
        List<Person> persons =  new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Person person = Person.builder().name("张三"+i).age(i).id(Long.valueOf(i)).build();
            persons.add(person);
        }
        mongoTemplate.insertAll(persons);
    }

}
