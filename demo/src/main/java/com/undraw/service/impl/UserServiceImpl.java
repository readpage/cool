package com.undraw.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.undraw.domain.dto.UserParam;
import com.undraw.domain.entity.User;
import com.undraw.handler.mybatis.EnhancedServiceImpl;
import com.undraw.mapper.UserMapper;
import com.undraw.service.UserService;
import com.undraw.util.excel.ExcelUtils;
import com.undraw.util.page.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
public class UserServiceImpl extends EnhancedServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> list(UserParam obj) {
        String sort = obj.sort(User.class, "id");
        return userMapper.selectList(Wrappers.lambdaQuery(User.class)
                .last(sort)
        );
    }

    @Override
    public PageInfo<User> page(UserParam obj) {
        PageHelper.startPage(obj.getCurrent(), obj.getSize());
        List<User> list = this.list(obj);
        return new PageInfo<>(list);
    }

    @Override
    public boolean upload(MultipartFile file) {
        List<User> list = ExcelUtils.read(file, User.class);
        return this.saveOrUpdateBatchByColumn(list, o -> Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, o.getUsername())
        );
    }

    @Override
    public void export(HttpServletResponse response, UserParam obj) {
        List<User> list = this.list(obj);
        ExcelUtils.export(response, "用户信息-" + LocalDate.now().toString(), list);
    }

    @Override
    public boolean badSql() {
        List<User> userList = userMapper.selectList(null);
        return this.saveOrUpdateBatchByColumn2(userList, o -> Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, o.getUsername())
                .eq(User::getPassword, o.getPassword())
                .eq(User::getCreateTime, o.getCreateTime())
        );
    }

    @Override
    public void test() {
        this.saveBatch(new ArrayList<>());
        System.out.println("service test");
    }

}
