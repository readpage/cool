package com.undraw.service;

import com.undraw.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:00
 */
public interface UserService extends IService<User> {
    boolean badSql();

    void test();
}
