package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.entity.SysConfig;

import java.util.List;

/**
 * <p>
 *  系统默认配置服务类
 * </p>
 */
public interface SysConfigService extends IService<SysConfig> {

    /** 按业务键 (configGroup, configKey) UPSERT */
    void upsert(SysConfig config);

    /** 查询系统默认配置 */
    SysConfig getSystemConfig(String configGroup, String configKey);

    /** 按分组列出所有系统默认配置 */
    List<SysConfig> listByGroup(String configGroup);
}
