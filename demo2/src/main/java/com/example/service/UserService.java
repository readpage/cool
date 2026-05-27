package com.example.service;

import com.example.domain.entity.User;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /** 动态筛选全量查询 */
    List<User> list(FilterParam param);

    /** 动态筛选分页查询 */
    PageResult<User> page(FilterParam param);

}
