package com.example.dao;

import com.example.domain.entity.SysConfig;
import com.example.template.annotation.Modify;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.annotation.SqlDao;

import java.util.List;

@SqlDao
public interface SysConfigDao {

    /**
     * 查系统默认配置 (user_id=0, deleted=0)
     */
    @Query("SELECT * FROM sys_config WHERE config_group = #{configGroup} AND config_key = #{configKey} AND user_id = 0 AND deleted = 0")
    SysConfig findSystem(@Param("configGroup") String configGroup, @Param("configKey") String configKey);

    /**
     * 查用户偏好配置 (deleted=0)
     */
    @Query("SELECT * FROM sys_config WHERE config_group = #{configGroup} AND config_key = #{configKey} AND user_id = #{userId} AND deleted = 0")
    SysConfig findUser(@Param("configGroup") String configGroup, @Param("configKey") String configKey, @Param("userId") Long userId);

    /**
     * 插入或更新配置 (ON DUPLICATE KEY UPDATE)
     */
    @Modify("INSERT INTO sys_config (config_group, config_key, user_id, config_value, version, deleted) " +
            "VALUES (#{configGroup}, #{configKey}, #{userId}, #{configValue}, #{version}, #{deleted}) " +
            "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), version = VALUES(version), deleted = VALUES(deleted)")
    int upsert(SysConfig config);

    /**
     * 根据 ID 查找配置
     */
    @Query("SELECT * FROM sys_config WHERE id = #{id}")
    SysConfig findById(@Param("id") Long id);

    /**
     * 软删除（移入回收站）
     */
    @Modify("UPDATE sys_config SET deleted = 1 WHERE id = #{id}")
    int softDelete(@Param("id") Long id);

    /**
     * 从回收站恢复
     */
    @Modify("UPDATE sys_config SET deleted = 0 WHERE id = #{id}")
    int restore(@Param("id") Long id);

    /**
     * 物理删除
     */
    @Modify("DELETE FROM sys_config WHERE id = #{id}")
    int physicalDelete(@Param("id") Long id);

    /**
     * 查询回收站列表
     */
    @Query("SELECT * FROM sys_config WHERE config_group = #{configGroup} AND config_key = #{configKey} AND deleted = 1")
    List<SysConfig> listDeleted(@Param("configGroup") String configGroup, @Param("configKey") String configKey);
}
