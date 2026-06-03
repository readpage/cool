package com.example.dao;

import com.example.domain.entity.SysConfig;
import com.example.template.annotation.Modify;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.annotation.SqlDao;

@SqlDao
public interface SysConfigDao {

    /** 查系统默认配置 */
    @Query("SELECT * FROM sys_config WHERE config_group = #{configGroup} AND config_key = #{configKey}")
    SysConfig findSystem(@Param("configGroup") String configGroup, @Param("configKey") String configKey);

    /** 插入或更新系统配置 (ON DUPLICATE KEY UPDATE) */
    @Modify("INSERT INTO sys_config (config_group, config_key, config_value, version) " +
            "VALUES (#{configGroup}, #{configKey}, #{configValue}, #{version}) " +
            "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), version = VALUES(version)")
    int upsert(SysConfig config);

    /** 根据 ID 查找系统配置 */
    @Query("SELECT * FROM sys_config WHERE id = #{id}")
    SysConfig findById(@Param("id") Long id);

    /** 物理删除 */
    @Modify("DELETE FROM sys_config WHERE id = #{id}")
    int physicalDelete(@Param("id") Long id);
}
