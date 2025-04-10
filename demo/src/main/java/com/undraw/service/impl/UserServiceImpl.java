package com.undraw.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.undraw.domain.dto.UserParam;
import com.undraw.domain.entity.User;
import com.undraw.mapper.UserMapper;
import com.undraw.service.UserService;
import com.undraw.util.page.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> list(UserParam obj) {
        return userMapper.selectList(null);
    }

    @Override
    public PageInfo<User> page(UserParam obj) {
        PageHelper.startPage(obj.getCurrent(), obj.getSize());
        List<User> list = this.list(obj);
        return new PageInfo<>(list);
    }

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
