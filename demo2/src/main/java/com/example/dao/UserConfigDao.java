package com.example.dao;

import com.example.domain.entity.UserConfig;
import com.example.template.annotation.Modify;
import com.example.template.annotation.Param;
import com.example.template.annotation.Query;
import com.example.template.annotation.SqlDao;

import java.util.List;

@SqlDao
public interface UserConfigDao {

    /** 查用户偏好配置 (deleted=0) */
    @Query("SELECT * FROM user_config WHERE config_group = #{configGroup} AND config_key = #{configKey} AND user_id = #{userId} AND deleted = 0")
    UserConfig findUser(@Param("configGroup") String configGroup, @Param("configKey") String configKey, @Param("userId") Long userId);

    /** 插入或更新用户配置 (ON DUPLICATE KEY UPDATE) */
    @Modify("INSERT INTO user_config (config_group, config_key, user_id, config_value, deleted) " +
            "VALUES (#{configGroup}, #{configKey}, #{userId}, #{configValue}, #{deleted}) " +
            "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), deleted = VALUES(deleted)")
    int upsert(UserConfig config);

    /** 根据 ID 查找用户配置 */
    @Query("SELECT * FROM user_config WHERE id = #{id}")
    UserConfig findById(@Param("id") Long id);

    /** 软删除 */
    @Modify("UPDATE user_config SET deleted = 1 WHERE id = #{id}")
    int softDelete(@Param("id") Long id);

    /** 从回收站恢复 */
    @Modify("UPDATE user_config SET deleted = 0 WHERE id = #{id}")
    int restore(@Param("id") Long id);

    /** 物理删除 */
    @Modify("DELETE FROM user_config WHERE id = #{id}")
    int physicalDelete(@Param("id") Long id);

    /** 查询回收站列表 */
    @Query("SELECT * FROM user_config WHERE config_group = #{configGroup} AND config_key = #{configKey} AND deleted = 1")
    List<UserConfig> listDeleted(@Param("configGroup") String configGroup, @Param("configKey") String configKey);
}
