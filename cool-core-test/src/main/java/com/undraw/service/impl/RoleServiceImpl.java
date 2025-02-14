package com.undraw.service.impl;

import com.undraw.domain.entity.Role;
import com.undraw.handler.EnhancedServiceImpl;
import com.undraw.mapper.RoleMapper;
import com.undraw.service.RoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author readpage
 * @since 2025-02-13 21:15
 */
@Service
public class RoleServiceImpl extends EnhancedServiceImpl<RoleMapper, Role> implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Override
    public boolean save(Role role) {
        return roleMapper.insert(role) > 0;
    }

    @Override
    public boolean saveBatch(List<Role> list) {
        boolean b = this.saveBatch1(list);
        return b;
    }
}
