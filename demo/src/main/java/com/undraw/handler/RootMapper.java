package com.undraw.handler;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface RootMapper<T> extends BaseMapper<T> {
    /**
     * 根据key查询查询数据
     * @param whereSql
     * @return
     */
    List<T> listByKey(String whereSql);

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
    int insertOrUpdateBatch(List<T> list);

}