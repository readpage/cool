package com.undraw.service;


import cn.undraw.model.Compare;
import com.undraw.mapper.provider.Query;
import com.undraw.util.page.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BiConsumer;

public interface ModelService {

    boolean save(Map<String, Object> map);

    boolean saveBatch(Map<String, Object> map);

    boolean update(Map<String, Object> map);

    boolean updateBatch(Map<String, Object> map);

    boolean saveOrUpdateBatch(Map<String, Object> map);

    boolean saveOrUpdateBatch(Map<String, Object> map, BiConsumer<List<Map<String, Object>>, List<Map<String, Object>>> con);

    boolean remove(Map<String, Object> map);

    boolean removeBatch(Map<String, Object> map);

    List<LinkedHashMap<String, Object>> listByKey(Map<String, Object> map);

    List<LinkedHashMap<String, Object>> listByKeys(Map<String, Object> map);

    List<LinkedHashMap<String, Object>> list(Map<String, Object> map);

    PageInfo<LinkedHashMap<String, Object>> page(Map<String, Object> map);

    boolean upload(String tableName, MultipartFile file);

    void export(HttpServletResponse response, @RequestParam Map<String, Object> map);

    String where(Query query);

    void where(Map<String, Object> map, Class<?> objClass);

    TreeSet<String> getColumnNames(List<String> tableNames);

    boolean sync(Compare compare, String tableName, String key);

    boolean sync(Compare compare, String tableName);
}
