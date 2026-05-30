package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.entity.User;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;

import java.util.List;

/**
 * 用户服务接口 — MyBatis-Plus IService 提供标准 CRUD
 */
public interface UserService extends IService<User> {

    /** 动态筛选全量查询（FilterParam → 自定义模板 DAO） */
    List<User> list(FilterParam param);

    /** 动态筛选分页查询（FilterParam → 自定义模板 DAO） */
    PageResult<User> page(FilterParam param);

    /** 批量导入用户，返回成功/失败统计（逐条 saveOrUpdate，后续优化为高性能批量 upsert） */
    ImportBatchResult importBatch(List<User> users);

}
