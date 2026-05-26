package com.example;

import com.example.dao.UserDao;
import com.example.domain.entity.User;
import com.example.template.util.FilterParam;
import com.example.template.util.SqlTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserDao 完整功能测试 —— 需要 MySQL 服务运行。
 *
 * 覆盖：@Query 动态条件 / @Modify 写入 / {{}} 动态表名 / 列提取 + 白名单校验
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest {

    @Resource
    private UserDao userDao;

    @Autowired
    private SqlTemplate sqlTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Long testId;

    private static final ObjectMapper mapper = new ObjectMapper();

    // ==================== 查询 ====================

    @Test
    @Order(1)
    void testList() {
        List<User> list = userDao.list();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        System.out.println("list() 共 " + list.size() + " 条，首条: " + list.get(0).getUsername());
    }

    @Test
    @Order(2)
    void testFindById() {
        // 取第一条数据
        User first = userDao.list().get(0);
        User u = userDao.findById(first.getId());
        assertNotNull(u);
        assertEquals(first.getUsername(), u.getUsername());
        System.out.println("findById(" + first.getId() + "): " + u.getUsername());
    }

    // ==================== 动态条件（[[]] 核心） ====================

    @Test
    @Order(3)
    void testFindByUsername() {
        // 只传 username，age 和 phone 条件自动丢弃
        List<User> list = userDao.findByCondition("admin", null, null);
        assertNotNull(list);
        System.out.println("findByCondition(username=admin): " + list.size() + " 条");
    }

    @Test
    @Order(4)
    void testFindByAge() {
        // 只传 age，其他条件自动丢弃
        List<User> list = userDao.findByCondition(null, 0, null);
        assertNotNull(list);
        System.out.println("findByCondition(age>0): " + list.size() + " 条");
    }

    @Test
    @Order(5)
    void testFindByCombined() {
        // 三个条件都有值
        List<User> list = userDao.findByCondition("test", 10, "138");
        System.out.println("findByCondition(组合): " + list.size() + " 条");
    }


    // ==================== {{filter}} / {{sort}} 动态 DAO ====================

    /**
     * 最简调用：JSON String → FilterParam → DAO。
     * 代理内部自动提取列白名单 + 生成 SQL，调用方零手动处理。
     */
    @Test
    @Order(7)
    void dynamicList() throws Exception {
        String json = """
                {
                    "filter": [
                        { "column": "age", "operator": "gt", "value": "22" },
                        { "column": "username", "operator": "contains", "value": "马" }
                    ],
                    "sort": { "column": "age", "direction": "desc" }
                }
                """;

        // 一行：String → FilterParam → dynamicList
        List<User> users = userDao.dynamicList(mapper.readValue(json, FilterParam.class));

        System.out.println(users);
    }

    /**
     * SQL 注入模拟 — column / operator / value 三层防御。
     */
    @Test
    @Order(8)
    void sqlInjection() throws Exception {
        // column 注入 → 白名单拦截
        FilterParam p1 = mapper.readValue("""
                { "filter": [{ "column": "1;DROP TABLE", "operator": "eq", "value": "x" }] }
                """, FilterParam.class);
        assertThrows(IllegalArgumentException.class, () -> userDao.dynamicList(p1));

        // operator 注入 → 枚举白名单拦截
        FilterParam p2 = mapper.readValue("""
                { "filter": [{ "column": "username", "operator": "1=1", "value": "admin" }] }
                """, FilterParam.class);
        assertThrows(IllegalArgumentException.class, () -> userDao.dynamicList(p2));

        // value 注入 → 参数化绑定
        FilterParam p3 = mapper.readValue("""
                { "filter": [{ "column": "username", "operator": "contains", "value": "' OR '1'='1" }] }
                """, FilterParam.class);
        List<User> users = userDao.dynamicList(p3);
        assertTrue(users.isEmpty(), "OR 1=1 注入被参数化绑定阻止");

        System.out.println("SQL 注入防御: column/operator/value all passed");
    }

    /**
     * DB 模板查询 — 只需 sql + param 两个参数。
     */
    @Test
    @Order(9)
    void dbTemplateQuery() throws Exception {
        String sql = """
            SELECT id, username, password, age, phone, create_time, update_time
            FROM user WHERE {{filter}}
            {{sort}}
            """;

        String json = """
                {
                    "filter": [
                        { "column": "age", "operator": "gt", "value": "22" },
                        { "column": "username", "operator": "contains", "value": "马" }
                    ],
                    "sort": { "column": "age", "direction": "desc" }
                }
                """;

        // 3. 只需 sql + param
        FilterParam param = mapper.readValue(json, FilterParam.class);

        List<User> users = sqlTemplate.query(sql, param, User.class);

        System.out.println(users);
    }

}

