package com.undraw.mongodb;

import com.mongodb.client.model.Indexes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
 
@SpringBootTest
public class IndexTest {
    @Autowired
    private MongoTemplate mongoTemplate;
 
    /**
     * 创建升序索引
     */
    @Test
    public void createAscendingIndex() {
        // 设置字段名称
        String field = "age";
        // 创建索引
        mongoTemplate.getCollection("person").createIndex(Indexes.descending(field));
    }
 
    /**
     * 根据索引名称移除索引
     */
    @Test
    public void removeIndex() {
        // 设置字段名称
        String field = "age_1";
        // 删除索引
        mongoTemplate.getCollection("person").dropIndex(field);
    }
 
 
    /**
     * 查询集合中所有的索引
     */
//    @Test
//    public void getIndexAll() {
//        // 获取集合中所有列表
//        ListIndexesIterable<org.bson.Document> indexList =   mongoTemplate.getCollection("person").listIndexes();
//        // 获取集合中全部索引信息
//        for (Document document : indexList) {
//            System.out.println("索引列表：" + document);
//        }
//    }
}