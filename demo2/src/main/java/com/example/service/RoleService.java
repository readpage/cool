package com.example.service;

import com.example.domain.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /** 获取所有角色列表 */
    List<Role> listAll();
}
