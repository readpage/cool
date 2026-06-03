package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表 Mapper
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
