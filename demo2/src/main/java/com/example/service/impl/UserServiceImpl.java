package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.UserDao;
import com.example.domain.dto.ImportBatchResult;
import com.example.domain.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.OptionService;
import com.example.service.UserService;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;
import com.example.template.util.SqlTemplate;
import com.example.util.excel.ExcelUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户服务实现 — MyBatis-Plus ServiceImpl 提供标准 CRUD，FilterParam 查询走自定义 DAO
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private SqlTemplate sqlTemplate;

    @Resource
    private OptionService optionService;

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
     * 批量导入用户 — INSERT ... ON DUPLICATE KEY UPDATE
     */
    @Override
    public ImportBatchResult importBatch(List<User> users) {
        if (users == null || users.isEmpty()) {
            return ImportBatchResult.empty();
        }

        int total = users.size();
        try {
            sqlTemplate.batchSaveOrUpdate(users);
            log.info("批量导入完成: total={}, success={}", total, total);
            return ImportBatchResult.of(total, total, 0, List.of());
        } catch (Exception e) {
            log.error("批量导入失败: total={}, error={}", total, e.getMessage());
            return ImportBatchResult.of(total, 0, total, List.of(e.getMessage()));
        }
    }

    @Override
    public ImportBatchResult importExcel(MultipartFile file, String filterParamJson) {
        List<User> users = ExcelUtils.importData(file, filterParamJson,
                (type, limit) -> optionService.listByType(type, limit), User.class);
        if (users.isEmpty()) {
            return ImportBatchResult.empty();
        }
        return importBatch(users);
    }

}
