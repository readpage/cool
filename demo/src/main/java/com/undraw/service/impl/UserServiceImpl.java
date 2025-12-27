package com.undraw.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.undraw.domain.dto.UserParam;
import com.undraw.domain.entity.User;
import com.undraw.mapper.UserMapper;
import com.undraw.service.ModelService;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ModelService modelService;

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
        return modelService.upload("user", file);
    }

    @Override
    public void export(HttpServletResponse response, UserParam obj) {
        List<User> list = this.list(obj);
        ExcelUtils.export(response, "用户信息-" + LocalDate.now().toString(), list);
    }

    @Override
    public boolean badSql() {
        List<User> userList = userMapper.selectList(null);
        return false;
    }

    @Override
    public void test() {
        this.saveBatch(new ArrayList<>());
        System.out.println("service test");
    }

}
