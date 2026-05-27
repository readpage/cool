package com.example.service.impl;

import com.example.dao.UserDao;
import com.example.domain.entity.User;
import com.example.service.UserService;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public List<User> list(FilterParam param) {
        return userDao.dynamicList(param);
    }

    @Override
    public PageResult<User> page(FilterParam param) {
        return userDao.page(param);
    }

}
