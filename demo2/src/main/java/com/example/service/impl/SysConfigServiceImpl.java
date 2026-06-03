package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.entity.SysConfig;
import com.example.mapper.SysConfigMapper;
import com.example.service.SysConfigService;
import com.example.template.util.SqlTemplate;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Resource
    private SqlTemplate sqlTemplate;

    @Override
    public void upsert(SysConfig config) {
        sqlTemplate.saveOrUpdate(config, "configGroup", "configKey", "userId", "deleted");
    }

    @Override
    public SysConfig getSystemConfig(String configGroup, String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getUserId, 0L)
               .eq(SysConfig::getDeleted, 0);
        if (configGroup != null) wrapper.eq(SysConfig::getConfigGroup, configGroup);
        if (configKey != null) wrapper.eq(SysConfig::getConfigKey, configKey);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public SysConfig getUserConfig(Long userId, String configGroup, String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getUserId, userId)
               .eq(SysConfig::getDeleted, 0);
        if (configGroup != null) wrapper.eq(SysConfig::getConfigGroup, configGroup);
        if (configKey != null) wrapper.eq(SysConfig::getConfigKey, configKey);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }
}
