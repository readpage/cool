package com.undraw.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.undraw.domain.entity.Role;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author readpage
 * @since 2025-02-13 21:15
 */
public interface RoleService extends IService<Role> {

    boolean save(Role role);

    boolean saveBatch(List<Role> list);

}
