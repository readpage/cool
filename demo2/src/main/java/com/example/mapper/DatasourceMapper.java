package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.entity.Datasource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源 Mapper
 */
@Mapper
public interface DatasourceMapper extends BaseMapper<Datasource> {
}
