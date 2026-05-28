package com.example.dao;

import com.example.domain.entity.Option;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.annotation.SqlDao;

import java.util.List;

/**
 * 选项 DAO — 提供远程搜索等自定义查询
 */
@SqlDao
public interface OptionDao {

    /**
     * 远程搜索 — 根据 type 和 keyword 模糊匹配 label，限制返回条数
     * 仅返回未删除的记录
     */
    @Query("""
        SELECT id, type, label, value, deleted, create_time as createTime, update_time as updateTime
        FROM options
        WHERE deleted = 0
          [[AND type = #{type}]]
          [[AND label LIKE CONCAT('%', #{keyword}, '%')]]
        ORDER BY id DESC
        LIMIT #{limit}
        """)
    List<Option> search(@Param("type") String type,
                        @Param("keyword") String keyword,
                        @Param("limit") Integer limit);

    /**
     * 根据 type 查询所有有效选项，限制返回条数
     */
    @Query("""
        SELECT id, type, label, value, deleted, create_time as createTime, update_time as updateTime
        FROM options
        WHERE deleted = 0
          [[AND type = #{type}]]
        ORDER BY id DESC
        LIMIT #{limit}
        """)
    List<Option> listByType(@Param("type") String type,
                            @Param("limit") Integer limit);
}
