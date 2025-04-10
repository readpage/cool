package com.undraw.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.undraw.domain.dto.UserParam;
import com.undraw.domain.entity.User;
import com.undraw.util.page.PageInfo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:00
 */
public interface UserService extends IService<User> {

    /**
     * 查询用户信息
     * @param obj
     * @return
     */
    List<User> list(UserParam obj);

    /**
     * 分页查询用户信息
     * @param obj
     * @return
     */
    PageInfo<User> page(UserParam obj);


    boolean badSql();

    void test();
}
