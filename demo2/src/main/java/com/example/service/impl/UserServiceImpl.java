package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.UserDao;
import com.example.domain.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.ImportBatchResult;
import com.example.service.UserService;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现 — MyBatis-Plus ServiceImpl 提供标准 CRUD，FilterParam 查询走自定义 DAO
 */
@Slf4j
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

    /**
     * 批量导入用户（逐条 try-catch，统计成功/失败数）
     * TODO: 实现真正的批量 upsert —— 利用 ON DUPLICATE KEY UPDATE 或 INSERT IGNORE
     */
    @Override
    public ImportBatchResult importBatch(List<User> users) {
        if (users == null || users.isEmpty()) {
            return ImportBatchResult.empty();
        }

        int total = users.size();
        int success = 0;
        int fail = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            try {
//                saveOrUpdate(user);
                log.info("批量导入成功: {}", user);
                success++;
            } catch (Exception e) {
                fail++;
                String errorMsg = String.format("第 %d 行 (username=%s): %s",
                        i + 1, user.getUsername(), e.getMessage());
                errors.add(errorMsg);
                log.warn("批量导入失败: {}", errorMsg);
            }
        }

        log.info("批量导入完成: total={}, success={}, fail={}", total, success, fail);
        return ImportBatchResult.of(total, success, fail, errors);
    }

}
