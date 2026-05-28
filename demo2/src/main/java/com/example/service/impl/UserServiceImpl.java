package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.UserDao;
import com.example.domain.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现 — MyBatis-Plus ServiceImpl 提供标准 CRUD，FilterParam 查询走自定义 DAO
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public List<User> list(FilterParam param) {
        return userDao.list(param);
    }

    @Override
    public PageResult<User> page(FilterParam param) {
        List<User> users = userDao.list(param.startPage());
        return new PageResult<>(param, users);
    }

}
