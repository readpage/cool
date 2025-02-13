package com.undraw.service.impl;

import com.undraw.domain.entity.Role;
import com.undraw.mapper.RoleMapper;
import com.undraw.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public boolean save(List<Role> list) {
        return false;
    }
}
