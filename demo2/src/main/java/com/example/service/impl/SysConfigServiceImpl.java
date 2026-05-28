package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.entity.SysConfig;
import com.example.mapper.SysConfigMapper;
import com.example.service.SysConfigService;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现 — MyBatis-Plus ServiceImpl 提供标准 CRUD
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {
}
