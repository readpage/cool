package com.undraw.handler;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

public interface RootMapper<T> extends BaseMapper<T> {
    /**
     * 根据key查询查询数据
     * @param map
     * @return
     */
    List<T> listByKey(Map map);

    /**
     * 自定义批量插入
     */
    int insertBatch(List<T> list);

    /**
     * 自定义批量更新
     */
    int updateBatch(List<T> list);

    /**
     * 自定义批量新增或更新(mysql)
     */
    int insertOrUpdateBath(List<T> list);

}