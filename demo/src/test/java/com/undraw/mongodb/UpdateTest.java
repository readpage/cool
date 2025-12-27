package com.undraw.mongodb;

import com.undraw.domain.entity.Person;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
 
@SpringBootTest
class UpdateTest {
    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 修改数据
     * 方法一
     */
    @Test
    public void save() {
        Person per = mongoTemplate.findById(1L, Person.class);
        per.setName("武松");
        mongoTemplate.save(per);
    }

    /**
     * 修改数据
     * 方法二
     */
    @Test
    public void updateFirst() {
        // 查询条件 查询id为1l
        Query query = new Query(Criteria.where("id").is(1L));
        // 修改内容
        Update update = new Update().set("name", "宋江");
        mongoTemplate.updateFirst(query, update, Person.class);
    }

    /**
     * 批量修改数据
     */
    @Test
    public void updateMulti() {
        // 查询条件  gte表示大于等于
        Query query = new Query(Criteria.where("id").gte(1L));
        // 修改内容
        Update update = new Update().set("name", "宋江");
        mongoTemplate.updateMulti(query, update, Person.class);
    }
}