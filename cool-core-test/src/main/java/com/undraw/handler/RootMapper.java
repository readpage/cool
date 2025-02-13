package com.undraw.handler;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface RootMapper<T> extends BaseMapper<T> {
    /**
     * 自定义批量插入
     */
    boolean insertBatch(List<T> list);

    /**
     * 自定义批量更新
     */
    boolean updateBatch(List<T> list);

    /**
     * 自定义批量新增或更新(mysql)
     */
    boolean insertOrUpdateBath(List<T> list);

}