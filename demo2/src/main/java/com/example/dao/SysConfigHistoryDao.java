package com.example.dao;

import com.example.domain.entity.SysConfigHistory;
import com.example.template.annotation.Modify;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.annotation.SqlDao;

import java.util.List;

@SqlDao
public interface SysConfigHistoryDao {

    /**
     * 按版本号查找历史快照
     */
    @Query("SELECT * FROM sys_config_history WHERE config_group = #{configGroup} AND config_key = #{configKey} AND version = #{version}")
    SysConfigHistory findByVersion(@Param("configGroup") String configGroup, @Param("configKey") String configKey, @Param("version") Integer version);

    /**
     * 查询版本历史列表（按版本号倒序）
     */
    @Query("SELECT * FROM sys_config_history WHERE config_group = #{configGroup} AND config_key = #{configKey} ORDER BY version DESC")
    List<SysConfigHistory> listHistory(@Param("configGroup") String configGroup, @Param("configKey") String configKey);

    /**
     * 插入历史记录
     */
    @Modify("INSERT INTO sys_config_history (config_group, config_key, version, snapshot, change_type, rollback_from, changed_by) " +
            "VALUES (#{configGroup}, #{configKey}, #{version}, #{snapshot}, #{changeType}, #{rollbackFrom}, #{changedBy})")
    int insert(SysConfigHistory history);
}
