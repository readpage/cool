package com.undraw.handler;

import cn.undraw.util.ConvertUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;

import java.util.*;
import java.util.function.Function;

public class EnhancedServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {


    public <T> boolean updateBatchByColumn(Collection<T> entityList, Function<T, Wrapper<T>> function) {
        String sqlStatement = getSqlStatement(SqlMethod.UPDATE);
        return executeBatch(entityList, (sqlSession, entity) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.ENTITY, entity);
            param.put(Constants.WRAPPER, function.apply(entity));
        });
    }

    public <T> boolean removeBatchByColumn(Collection<T> entityList, Function<T, Wrapper<T>> function) {
        String sqlStatement = getSqlStatement(SqlMethod.DELETE);
        return executeBatch(entityList, (sqlSession, entity) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.ENTITY, entity);
            param.put(Constants.WRAPPER, function.apply(entity));
            sqlSession.delete(sqlStatement, param);
        });
    }

    /**
     * 可以根据其他字段批量更新或新增
     *
     * @param entityList 数据集合
     * @param function   新增或者更新判断条件
     * @return 操作结果
     */
    public boolean saveOrUpdateBatchByColumn(Collection<T> entityList, Function<T, Wrapper<T>> function) {
        List<List<T>> lists = ConvertUtils.batchList(entityList, 2);
        return executeBatch(lists, (sqlSession, list) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.LIST, list);
            String sqlStatement = this.mapperClass.getName() + "." + "insertOrUpdateBath";
            sqlSession.insert(sqlStatement, param);
        });
    }

    public boolean saveOrUpdateBatchByColumn2(Collection<T> entityList, Function<T, Wrapper<T>> function) {
        return SqlHelper.saveOrUpdateBatch(this.entityClass, this.mapperClass, this.log, entityList, DEFAULT_BATCH_SIZE, (sqlSession, entity) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.ENTITY, entity);
            param.put(Constants.WRAPPER, function.apply(entity));
            return CollectionUtils.isEmpty(sqlSession.selectList(this.getSqlStatement(SqlMethod.SELECT_MAPS), param));
        }, (sqlSession, entity) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.ENTITY, entity);
            param.put(Constants.WRAPPER, function.apply(entity));
            sqlSession.update(this.getSqlStatement(SqlMethod.UPDATE), param);
        });
    }

    private final int PARAM_SIZE = 50;

    public boolean saveOrUpdateBatchByColumn(Collection<T> entityList, int paramSize) {
        List<List<T>> lists = ConvertUtils.batchList(entityList, 2);
        return executeBatch(lists, (sqlSession, list) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.LIST, list);
            String sqlStatement = this.mapperClass.getName() + "." + "insertOrUpdateBath";
            sqlSession.insert(sqlStatement, param);
        });

    }

    public boolean saveOrUpdateBatchByColumn(Collection<T> entityList) {
        return this.saveOrUpdateBatchByColumn(entityList, PARAM_SIZE);
    }

}
