package com.undraw.mapper.provider;

import org.apache.ibatis.annotations.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ModelMapper<T> {

    @InsertProvider(type = ModelProvider.class, method = "insert")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Model model);

    @InsertProvider(type = ModelProvider.class, method = "insertBatch")
    int insertBatch(Model model);

    @UpdateProvider(type = ModelProvider.class, method = "updateBatch")
    int updateBatch(Model model);

    @DeleteProvider(type = ModelProvider.class, method = "delete")
    int delete(@Param("model") Model model);

    @DeleteProvider(type = ModelProvider.class, method = "deleteBatch")
    int deleteBatch(Model model);

    @SelectProvider(type = ModelProvider.class, method = "list")
    List<LinkedHashMap<String, Object>> list(Model model);

    @SelectProvider(type = ModelProvider.class, method = "getOne")
    LinkedHashMap<String, Object> getOne(Model model);

    @SelectProvider(type = ModelProvider.class, method = "listByKeys")
    List<LinkedHashMap<String, Object>> listByKeys(Model model);

    @SelectProvider(type = ModelProvider.class, method = "listByKey")
    List<LinkedHashMap<String, Object>> listByKey(@Param("model") Model model);

    @Select("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = #{tableName} ORDER BY ORDINAL_POSITION")
    List<Map<String, Object>> columnList(String tableName);

    @Select("SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = #{tableName} LIMIT 1")
    Map<String, Object> table(String tableName);

}
