package com.undraw.mongodb;

import com.undraw.domain.entity.Person;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class QueryTest {
    @Resource
    private MongoTemplate mongoTemplate;
 
    /**
     * 查询集合中的全部文档数据
     */
    @Test
    public void findAll() {
        List<Person> personList = mongoTemplate.findAll(Person.class);
        System.out.println("查询结果：" + personList.toString());
    }

    @Test
    public void page() {
        int pageNum = 1;
        int pageSize = 2;
        Query query = new Query();
        long count = mongoTemplate.count(query, Person.class);
        query.skip((pageNum-1)*pageSize).limit(pageSize);
        List<Person> list = mongoTemplate.find(query, Person.class);
        System.out.println(list.toString());
        System.out.println(count);
    }

    /**
     * 查询集合中指定的ID文档数据
     */
    @Test
    public void findById() {
        Person byId = mongoTemplate.findById(3L, Person.class);
        System.out.println("查询结果：" + byId.toString());
    }
 
    /**
     * 根据条件查询符合条件的文档数据并返回第一条数据
     */
    @Test
    public void findOne() {
        Query query = new Query(Criteria.where("name").is("宋江1"));
        Person result = mongoTemplate.findOne(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 根据条件查询所有符合条件的文档
     */
    @Test
    public void findByCondition() {
        Query query = new Query(Criteria.where("age").gt(18));
        List<Person> result = mongoTemplate.find(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 根据【AND】关联多个查询条件，查询集合中所有符合条件的文档数据
     */
    @Test
    public void findByAndCondition() {
        // 创建条件
        Criteria name = Criteria.where("name").is("张三");
        Criteria age = Criteria.where("age").is(18);
        // 创建条件对象，将上面条件进行 AND 关联
        Criteria criteria = new Criteria().andOperator(name, age);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        List<Person> result = mongoTemplate.find(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 根据【OR】关联多个查询条件，查询集合中的文档数据
     */
    @Test
    public void findByOrCondition() {
        // 创建条件
        Criteria criteriaUserName = Criteria.where("name").is("张三");
        Criteria criteriaPassWord = Criteria.where("age").is(22);
        // 创建条件对象，将上面条件进行 OR 关联
        Criteria criteria = new Criteria().orOperator(criteriaUserName, criteriaPassWord);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        List<Person> result = mongoTemplate.find(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 根据【IN】关联多个查询条件，查询集合中的文档数据
     */
    @Test
    public void findByInCondition() {
        // 设置查询条件参数
        List<Long> ids = Arrays.asList(10L, 11L, 12L);
        // 创建条件
        Criteria criteria = Criteria.where("id").in(ids);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        List<Person> result = mongoTemplate.find(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 根据【逻辑运算符】查询集合中的文档数据
     */
    @Test
    public void findByOperator() {
        // 设置查询条件参数
        int min = 20;
        int max = 35;
        Criteria criteria = Criteria.where("age").gt(min).lte(max);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        List<Person> result = mongoTemplate.find(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 根据【正则表达式】查询集合中的文档数据
     */
    @Test
    public void findByRegex() {
        // 设置查询条件参数
        String regex = "^张";
        Criteria criteria = Criteria.where("name").regex(regex);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        List<Person> result = mongoTemplate.find(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 根据条件查询集合中符合条件的文档，获取其文档列表并排序
     */
    @Test
    public void findByConditionAndSort() {
        Query query = new Query(Criteria.where("name").is("张三")).with(Sort.by("age"));
        List<Person> result = mongoTemplate.find(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 根据单个条件查询集合中的文档数据，并按指定字段进行排序与限制指定数目
     */
    @Test
    public void findByConditionAndSortLimit() {
        String userName = "张三";
        //从第5行开始，查询3条数据返回
        Query query = new Query(Criteria.where("name").is("张三"))
                .with(Sort.by("createTime"))
                .limit(3).skip(5);
        List<Person> result = mongoTemplate.find(query, Person.class);
        System.out.println("查询结果：" + result.toString());
    }
 
    /**
     * 统计集合中符合【查询条件】的文档【数量】
     */
    @Test
    public void countNumber() {
        // 设置查询条件参数
        String regex = "^张*";
        Criteria criteria = Criteria.where("name").regex(regex);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        long count = mongoTemplate.count(query, Person.class);
        System.out.println("统计结果：" + count);
    }
 
}