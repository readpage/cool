package com.example;

import com.example.dao.TemplateTestConfig;
import com.example.dao.User2Dao;
import com.example.template.core.SqlTemplateEngine;
import com.example.template.util.ColumnExtractor;
import com.example.template.util.FilterOperator;
import com.example.template.util.FilterOperator.FilterCondition;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Query 模板引擎测试类。
 *
 * 使用 H2 内存数据库，无需外部 MySQL，开箱即跑。
 */
@SpringBootTest(
    classes = TemplateTestConfig.class,
    properties = {
        // 用 H2 内存数据库覆盖 MySQL 配置
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:test-schema.sql",
        "spring.sql.init.data-locations=classpath:test-data.sql",
        "cool.sql.logging=true"
    }
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Demo2ApplicationTests {

    @Autowired
    private User2Dao userDao;

    // ==================== 基础查询 ====================

    @Test
    @Order(1)
    void testFindAll() {
        List<Map<String, Object>> users = userDao.findAll();
        assertNotNull(users);
        assertTrue(users.size() >= 3, "应该至少有 3 条初始数据");
        System.out.println("findAll(): " + users);
    }


    // ==================== 单条查询 ====================

    @Test
    @Order(7)
    void testFindById() {
        Map<String, Object> user = userDao.findById(1L);
        assertNotNull(user);
        assertEquals(1L, user.get("id"));
        System.out.println("findById(1): " + user);
    }

    @Test
    @Order(2)
    void testAllFromTable() {
        // {{table}} 动态表名
        List<Map<String, Object>> users = userDao.findAllFromTable("users");
        assertNotNull(users);
        assertEquals(3, users.size());
        System.out.println("findAllFromTable('users'): " + users);
    }

    // ==================== 可选条件 ====================

    @Test
    @Order(3)
    void testConditionUsernameOnly() {
        // 只传 username，[[AND age > #{age}]] 因 age=null 被丢弃
        List<Map<String, Object>> result = userDao.findByCondition("Alice", null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).get("username"));
        System.out.println("condition(username=Alice, age=null): " + result);
    }

    @Test
    @Order(4)
    void testConditionAgeOnly() {
        // 只传 age，[[AND username = #{username}]] 被丢弃
        List<Map<String, Object>> result = userDao.findByCondition(null, 20);
        assertNotNull(result);
        // Bob(25) + Charlie(30) 应该都 > 20
        assertTrue(result.size() >= 2);
        System.out.println("condition(username=null, age=20): " + result);
    }

    @Test
    @Order(5)
    void testConditionBoth() {
        List<Map<String, Object>> result = userDao.findByCondition("Bob", 20);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).get("username"));
        System.out.println("condition(username=Bob, age=20): " + result);
    }

    @Test
    @Order(6)
    void testConditionNone() {
        // 都不传 → 所有可选块都被丢弃 → "WHERE 1=1" 查全表
        List<Map<String, Object>> result = userDao.findByCondition(null, null);
        assertNotNull(result);
        assertEquals(3, result.size());
        System.out.println("condition(null, null): " + result);
    }

    @Test
    @Order(8)
    void testFindByIdNotFound() {
        Map<String, Object> user = userDao.findById(999L);
        assertNull(user);
    }

    // ==================== 写入操作 ====================

    @Test
    @Order(9)
    void testInsert() {
        int rows = userDao.insert("Dave", 28);
        assertEquals(1, rows);

        // 验证插入成功
        List<Map<String, Object>> all = userDao.findAll();
        assertEquals(4, all.size());
        System.out.println("insert(Dave, 28): total=" + all.size());
    }

    @Test
    @Order(10)
    void testInsertBatch() {
        // 构造批量 VALUES 片段: ('Eve', 22), ('Frank', 35), ('Grace', 27)
        String values = "('Eve', 22), ('Frank', 35), ('Grace', 27)";
        int rows = userDao.insertBatch(values);
        assertEquals(3, rows);

        // 验证：之前 insert 了 1 条 (Dave) + 初始 3 条 + 批量 3 条 = 7
        List<Map<String, Object>> all = userDao.findAll();
        assertEquals(7, all.size());
        System.out.println("insertBatch: rows=" + rows + ", total=" + all.size());
    }

    @Test
    @Order(11)
    void testDeleteById() {
        int rows = userDao.deleteById(1L);
        assertEquals(1, rows);

        // 验证删除成功
        Map<String, Object> user = userDao.findById(1L);
        assertNull(user);
        System.out.println("deleteById(1): rows=" + rows);
    }

    // ==================== 列提取 + 条件构建 ====================

    @Test
    @Order(12)
    void testExtractColumns() {
        SqlTemplateEngine engine = new SqlTemplateEngine();

        // 基础提取
        assertEquals(List.of("id", "username", "age"),
                engine.extractColumns("SELECT id, username, age FROM users"));

        // AS 别名 → 取原名
        assertEquals(List.of("id", "name", "age"),
                engine.extractColumns("SELECT id, username AS name, age FROM users"));

        // 表前缀 → 去前缀
        assertEquals(List.of("id", "name"),
                engine.extractColumns("SELECT u.id, u.name FROM users u"));

        // * 通配符 → 返回空
        assertTrue(engine.extractColumns("SELECT * FROM users").isEmpty());

        // 含模板语法
        assertEquals(List.of("id", "name", "age"),
                engine.extractColumns("SELECT id, name, age FROM {{table}} WHERE 1=1 [[AND name=#{name}]]"));

        System.out.println("extractColumns: all passed");
    }

    @Test
    @Order(13)
    void testColumnValidation() {
        List<String> allowed = List.of("id", "name", "age", "status");

        // 白名单中的列 → 通过
        assertDoesNotThrow(() -> ColumnExtractor.checkColumn(allowed, "name"));
        assertDoesNotThrow(() -> ColumnExtractor.checkColumn(allowed, "age"));

        // 不在白名单中 → 抛异常
        assertThrows(IllegalArgumentException.class, () ->
                ColumnExtractor.checkColumn(allowed, "password"));

        // 空白名单（* 场景）→ 不校验
        assertDoesNotThrow(() -> ColumnExtractor.checkColumn(List.of(), "anything"));

        System.out.println("columnValidation: all passed");
    }

    @Test
    @Order(14)
    void testFilterOperator() {
        Map<String, Object> out = new LinkedHashMap<>();
        AtomicInteger idx = new AtomicInteger(0);

        // EQ
        out.clear(); idx.set(0);
        assertEquals("name = :name_0", FilterOperator.EQ.toSql("name", "Tom", "name_", idx, out));
        assertEquals(Map.of("name_0", "Tom"), out);

        // CONTAINS
        out.clear(); idx.set(0);
        assertEquals("name LIKE CONCAT('%', :name_0, '%')",
                FilterOperator.CONTAINS.toSql("name", "李", "name_", idx, out));
        assertEquals(Map.of("name_0", "李"), out);

        // BETWEEN
        out.clear(); idx.set(0);
        assertEquals("age BETWEEN :age_0 AND :age_1",
                FilterOperator.BETWEEN.toSql("age", List.of(25, 45), "age_", idx, out));
        assertEquals(Map.of("age_0", 25, "age_1", 45), out);

        // IN
        out.clear(); idx.set(0);
        assertEquals("dept IN (:dept_0, :dept_1)",
                FilterOperator.IN.toSql("dept", List.of("研发", "产品"), "dept_", idx, out));
        assertEquals(Map.of("dept_0", "研发", "dept_1", "产品"), out);

        System.out.println("filterOperator: all passed");
    }

    @Test
    @Order(15)
    void testFilterBuilderWithColumnCheck() {
        List<String> allowedColumns = List.of("id", "name", "age", "dept", "status");

        List<FilterCondition> filters = List.of(
                new FilterCondition("name",   FilterOperator.CONTAINS, "李"),
                new FilterCondition("age",    FilterOperator.BETWEEN,  List.of(25, 45)),
                new FilterCondition("dept",   FilterOperator.IN,       List.of("研发", "产品")),
                new FilterCondition("status", FilterOperator.NE,       "离职")
        );

        // 校验列名
        assertDoesNotThrow(() ->
                filters.forEach(f -> ColumnExtractor.checkColumn(allowedColumns, f.column())));

        // 生成 WHERE（:column_N 命名参数）
        Map<String, Object> out = new LinkedHashMap<>();
        AtomicInteger idx = new AtomicInteger(0);
        StringBuilder where = new StringBuilder("WHERE 1=1");
        for (FilterCondition f : filters) {
            where.append(" AND ");
            where.append(f.operator().toSql(f.column(), f.value(), f.column() + "_", idx, out));
        }

        System.out.println("Filter SQL : " + where);
        System.out.println("Params    : " + out);

        assertEquals("WHERE 1=1 AND name LIKE CONCAT('%', :name_0, '%') AND age BETWEEN :age_0 AND :age_1 AND dept IN (:dept_0, :dept_1) AND status != :status_0",
                where.toString());
        assertEquals(Map.of("name_0", "李", "age_0", 25, "age_1", 45, "dept_0", "研发", "dept_1", "产品", "status_0", "离职"), out);
    }

    // ==================== 初始化/清理 ====================

    @BeforeAll
    static void setUpClass() {
        System.out.println("\n========== @Query 模板引擎测试开始 ==========\n");
    }

    @AfterAll
    static void tearDownClass() {
        System.out.println("\n========== @Query 模板引擎测试完毕 ==========\n");
    }
}
