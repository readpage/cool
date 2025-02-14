package com.undraw.service.impl;

import com.undraw.domain.entity.User;
import com.undraw.mapper.UserMapper;
import com.undraw.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:00
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public boolean badSql() {
        userMapper.badSql();
        return false;
    }

    @Override
    public void test() {
        this.saveBatch(new ArrayList<>());
        System.out.println("service test");
    }

}
