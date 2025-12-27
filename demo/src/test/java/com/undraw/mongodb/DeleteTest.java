package com.undraw.mongodb;

import com.mongodb.client.result.DeleteResult;
import com.undraw.domain.entity.Person;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
 
@SpringBootTest
class DeleteTest {

    @Resource
    private MongoTemplate mongoTemplate;
 
    /**
     * 删除id 为1的记录
     */
    @Test
    void remove() {
        Query query = new Query(Criteria.where("id").is(1L));
        DeleteResult remove = mongoTemplate.remove(query, Person.class);
        System.out.println("删除的条数为：" + remove.getDeletedCount());//1
    }

    /**
     * 删除符合条件的所有文档
     */
    @Test
    public void remove2() {
        //删除年龄小于18的所有人: lt 表示小于等于
        Query query = new Query(Criteria.where("age").lt(18));
        DeleteResult result = mongoTemplate.remove(query, Person.class);
        System.out.println("删除条数：" + result.getDeletedCount());
    }

    /**
     * 删除符合条件的单个文档 并返回删除的文档
     */
    @Test
    public void findAndRemove() throws Exception {
        Query query = new Query(Criteria.where("id").is(2L));
        Person per = mongoTemplate.findAndRemove(query, Person.class);
        System.out.println(per);//Person(id=2, name=宋江, age=2)
    }

    /**
     * 删除符合条件的所有文档
     * 并返回删除的所有文档
     */
    @Test
    public  void findAllAndRemove(){
        // lt 表示小于
        Query query = new Query(Criteria.where("age").lt(3));
        List<Person> pers = mongoTemplate.findAllAndRemove(query, Person.class);
        System.out.println(pers);
    }
}