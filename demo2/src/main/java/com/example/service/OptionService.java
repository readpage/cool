package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.entity.Option;

import java.util.List;

/**
 * 选项服务接口 — MyBatis-Plus IService 提供标准 CRUD
 */
public interface OptionService extends IService<Option> {

    /**
     * 远程搜索 — 根据 type 和 keyword 模糊匹配 label，限制返回条数
     */
    List<Option> search(String type, String keyword, Integer limit);

    /**
     * 根据 type 查询所有有效选项，限制返回条数
     */
    List<Option> listByType(String type, Integer limit);
}
