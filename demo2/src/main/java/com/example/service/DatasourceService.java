package com.example.service;

import com.example.domain.entity.Datasource;

import java.util.List;

/**
 * 数据源管理服务
 */
public interface DatasourceService {

    /** 列出所有数据源 */
    List<Datasource> list();

    /** 按 ID 查询 */
    Datasource getById(Long id);

    /** 新增或更新（有 id 则更新，无则新增） */
    void save(Datasource ds);

    /** 按 ID 删除（软删除） */
    void deleteById(Long id);

    /**
     * 测试数据库连接。
     * @return null 表示连接成功；非 null 为错误描述
     */
    String testConnect(Datasource ds);
}
