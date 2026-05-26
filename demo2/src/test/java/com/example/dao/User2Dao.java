package com.example.dao;

import com.example.template.annotation.Modify;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.annotation.SqlDao;

import java.util.List;
import java.util.Map;

/**
 * 用户 DAO —— 演示 @Query 注解的各种语法。
 */
@SqlDao
public interface User2Dao {

    // ========== 基础查询 ==========

    /** 固定表名，无条件查询全部 */
    @Query("SELECT * FROM users")
    List<Map<String, Object>> findAll();

    /** {{}} 演示：动态表名 */
    @Query("SELECT * FROM {{table}}")
    List<Map<String, Object>> findAllFromTable(@Param("table") String table);

    // ========== 可选条件 ==========

    /** [[]] + #{} 演示：动态 WHERE */
    @Query("""
        SELECT id, username, age
        FROM users
        WHERE 1=1
          [[AND username = #{username}]]
          [[AND age > #{age}]]
        """)
    List<Map<String, Object>> findByCondition(@Param("username") String username,
                                              @Param("age") Integer age);

    // ========== 单条查询 ==========

    @Query("SELECT * FROM users WHERE id = #{id}")
    Map<String, Object> findById(@Param("id") Long id);

    // ========== 写入（@Modify）==========

    @Modify("INSERT INTO users (username, age) VALUES (#{username}, #{age})")
    int insert(@Param("username") String username, @Param("age") Integer age);

    /** 批量插入 —— {{values}} 直拼多行 VALUES */
    @Modify("INSERT INTO users (username, age) VALUES {{values}}")
    int insertBatch(@Param("values") String values);

    @Modify("DELETE FROM users WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
