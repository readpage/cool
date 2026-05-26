package com.example.dao;

import com.example.domain.entity.User;
import com.example.template.annotation.Modify;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.annotation.SqlDao;
import com.example.template.util.FilterParam;

import java.util.List;

/**
 * 用户 DAO —— 完整演示 @Query / @Modify 的各种语法。
 */
@SqlDao
public interface UserDao {

    // ==================== 查询（@Query）====================

    /** 无条件查全表 */
    @Query("SELECT id, username, password, age, phone, create_time as createTime, update_time as updateTime FROM user")
    List<User> list();

    /** 按 ID 查单条 */
    @Query("SELECT id, username, password, age, phone, create_time as createTime, update_time as updateTime FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * 动态条件查询 —— [[]] 拼 WHERE，三个条件任意组合。
     * username=null 时 [[AND username LIKE ...]] 自动丢弃，
     * age=null    时 [[AND age > ...]] 自动丢弃。
     */
    @Query("""
        SELECT id, username, password, age, phone, create_time as createTime, update_time as updateTime
        FROM user
        WHERE 1=1
          [[AND username LIKE CONCAT('%', #{username}, '%')]]
          [[AND age > #{age}]]
          [[AND phone LIKE CONCAT('%', #{phone}, '%')]]
        """)
    List<User> findByCondition(@Param("username") String username,
                               @Param("age") Integer age,
                               @Param("phone") String phone);

    /**
     * 动态筛选查询 — 传入 FilterParam，代理自动提列白名单并生成 SQL。
     *
     * <pre>{@code
     * String json = """
     *     { "filter": [{ "column": "username", "operator": "contains", "value": "admin" }],
     *       "sort": { "column": "id", "direction": "desc" } }""";
     * FilterParam params = mapper.readValue(json, FilterParam.class);
     * List<User> users = userDao.dynamicList(params);
     * }</pre>
     */
    @Query("""
            SELECT id, username, password, age, phone, create_time, update_time
            FROM user WHERE {{filter}}
            {{sort}}
            """)
    List<User> dynamicList(FilterParam params);

    // ==================== 写入（@Modify）====================

    @Modify("""
        INSERT INTO user (username, password, age, phone, create_time)
        VALUES (#{username}, #{password}, #{age}, #{phone}, NOW())
        """)
    int insert(@Param("username") String username,
               @Param("password") String password,
               @Param("age") Integer age,
               @Param("phone") String phone);

    @Modify("""
        UPDATE user SET
          username = #{username},
          password = #{password},
          age = #{age},
          phone = #{phone},
          update_time = NOW()
        WHERE id = #{id}
        """)
    int update(@Param("id") Long id,
               @Param("username") String username,
               @Param("password") String password,
               @Param("age") Integer age,
               @Param("phone") String phone);

    @Modify("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}

