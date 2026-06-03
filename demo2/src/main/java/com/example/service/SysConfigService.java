package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.entity.SysConfig;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 按业务键 (configGroup, configKey, userId, deleted) UPSERT
     */
    void upsert(SysConfig config);

    /**
     * 查询系统默认配置（userId=0, deleted=0）
     */
    SysConfig getSystemConfig(String configGroup, String configKey);

    /**
     * 查询用户配置（指定 userId, deleted=0）
     */
    SysConfig getUserConfig(Long userId, String configGroup, String configKey);
}
