package com.example.dao;

import com.example.domain.entity.Report;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.annotation.SqlDao;

import java.util.List;

/**
 * 报表 DAO — 权限过滤查询
 */
@SqlDao
public interface ReportDao {

    /**
     * 查询当前用户有权访问的报表列表。
     * <ul>
     *   <li>roleIds = [] → 仅创建者可访问：JSON_LENGTH(roleIds)=0 AND creator_id=userId</li>
     *   <li>roleIds 有值 → 用户角色与报表角色有交集：JSON_OVERLAPS</li>
     * </ul>
     */
    @Query("""
        SELECT id, table_key, name, description, category, display_type,
               datasource_id, creator_id, update_time
        FROM report
        WHERE deleted = 0
          AND (
            creator_id = #{userId}
            OR JSON_OVERLAPS(permission_config->'$.roleIds', CAST(#{roleIdsJson} AS JSON))
          )
        ORDER BY update_time DESC
        """)
    List<Report> listAccessibleReports(@Param("userId") Long userId,
                                       @Param("roleIdsJson") String roleIdsJson);
}
