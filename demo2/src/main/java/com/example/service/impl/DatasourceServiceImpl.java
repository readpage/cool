package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.domain.entity.Datasource;
import com.example.mapper.DatasourceMapper;
import com.example.service.DatasourceService;
import com.example.template.datasource.DynamicJdbcFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据源管理服务实现
 */
@Slf4j
@Service
public class DatasourceServiceImpl implements DatasourceService {

    @Resource
    private DatasourceMapper datasourceMapper;

    @Override
    public List<Datasource> list() {
        return datasourceMapper.selectList(
            new LambdaQueryWrapper<Datasource>()
                .orderByDesc(Datasource::getUpdateTime)
        );
    }

    @Override
    public Datasource getById(Long id) {
        return datasourceMapper.selectById(id);
    }

    @Override
    public void save(Datasource ds) {
        if (ds.getId() != null && datasourceMapper.selectById(ds.getId()) != null) {
            datasourceMapper.updateById(ds);
        } else {
            ds.setId(null);
            ds.setStatus(ds.getStatus() != null ? ds.getStatus() : 1);
            datasourceMapper.insert(ds);
        }
    }

    @Override
    public void deleteById(Long id) {
        datasourceMapper.deleteById(id);
    }

    @Override
    public String testConnect(Datasource ds) {
        return DynamicJdbcFactory.testConnect(ds);
    }
}
