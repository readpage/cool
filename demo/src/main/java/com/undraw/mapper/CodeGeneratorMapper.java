package com.undraw.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CodeGeneratorMapper {

    @Select("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = #{tableName}")
    List<Map<String, String>> listColumn(String tableName);


    @Select(" SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = #{tableName} LIMIT 1")
    Map<String, String> getTable(String tableName);
}
