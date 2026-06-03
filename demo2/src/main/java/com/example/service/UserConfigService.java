package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.entity.UserConfig;

import java.util.List;

/**
 * <p>
 *  用户偏好配置服务类
 * </p>
 */
public interface UserConfigService extends IService<UserConfig> {

    /** 按业务键 (configGroup, configKey, userId) UPSERT */
    void upsert(UserConfig config);

    /** 查询用户配置（指定 userId, deleted=0） */
    UserConfig getUserConfig(Long userId, String configGroup, String configKey);

    /** 按分组和用户列出所有用户配置 */
    List<UserConfig> listByUserAndGroup(Long userId, String configGroup);
}
