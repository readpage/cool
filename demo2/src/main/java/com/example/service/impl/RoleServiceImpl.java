package com.example.service.impl;

import com.example.domain.entity.Role;
import com.example.mapper.RoleMapper;
import com.example.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Override
    public List<Role> listAll() {
        return roleMapper.selectList(null);
    }
}
