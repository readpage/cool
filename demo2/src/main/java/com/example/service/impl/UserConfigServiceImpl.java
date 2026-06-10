package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.entity.UserConfig;
import com.example.mapper.UserConfigMapper;
import com.example.service.UserConfigService;
import com.example.template.util.SqlTemplate;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  用户偏好配置服务实现类
 * </p>
 */
@Service
public class UserConfigServiceImpl extends ServiceImpl<UserConfigMapper, UserConfig> implements UserConfigService {

    @Resource
    private SqlTemplate sqlTemplate;

    @Override
    public void upsert(UserConfig config) {
        sqlTemplate.saveOrUpdate(config, "configGroup", "configKey", "userId");
    }

    @Override
    public UserConfig getUserConfig(Long userId, String configGroup, String configKey) {
        LambdaQueryWrapper<UserConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConfig::getUserId, userId)
               .eq(UserConfig::getDeleted, 0);
        if (configGroup != null) wrapper.eq(UserConfig::getConfigGroup, configGroup);
        if (configKey != null) wrapper.eq(UserConfig::getConfigKey, configKey);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public List<UserConfig> listByUserAndGroup(Long userId, String configGroup) {
        LambdaQueryWrapper<UserConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConfig::getUserId, userId)
               .eq(UserConfig::getDeleted, 0);
        if (configGroup != null) wrapper.eq(UserConfig::getConfigGroup, configGroup);
        wrapper.orderByDesc(UserConfig::getUpdateTime);
        return this.list(wrapper);
    }
}
