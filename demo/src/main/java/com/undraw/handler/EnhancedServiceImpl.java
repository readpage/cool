package com.undraw.handler;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.bean.BeanUtils;
import cn.undraw.util.bean.SFunction;
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

    private final int BATCH_SIZE = 200;

    public List<T> listByKey(Collection<T> entityList, SFunction<T, ?>...fun) {
        List list = new ArrayList();
        if (entityList != null && entityList.size() > 0) {
            String fieldName = BeanUtils.getFieldName(fun);
            List<String> fieldNames = StrUtils.toList(fieldName, ",");
            List<List<T>> lists = ConvertUtils.batchList(entityList, BATCH_SIZE / fieldNames.size());
            for (List<T> list2 : lists) {
                StringBuffer sb = new StringBuffer();
                sb.append("WHERE (");
                sb.append(fieldName);
                sb.append(") IN (");
                for (T entity : list2) {
                    sb.append("(");
                    for (String name : fieldNames) {
                        Object fieldValue = BeanUtils.getFieldValue(entity, name);
                        sb.append("'").append(fieldValue).append("'").append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    sb.append(")").append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                sb.append(")");
                RootMapper rootMapper = (RootMapper) this.baseMapper;
                list.addAll(rootMapper.listByKey(sb.toString()));
            }
        }
        return list;
    }

    public <T, U> boolean updateBatchByColumn(Collection<T> entityList, Function<T, Wrapper<U>> function) {
        String sqlStatement = getSqlStatement(SqlMethod.UPDATE);
        return executeBatch(entityList, (sqlSession, entity) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.ENTITY, entity);
            param.put(Constants.WRAPPER, function.apply(entity));
            sqlSession.update(sqlStatement, param);
        });
    }

    public <T, U> boolean removeBatchByColumn(Collection<T> entityList, Function<T, Wrapper<U>> function) {
        String sqlStatement = getSqlStatement(SqlMethod.DELETE);
        return executeBatch(entityList, (sqlSession, entity) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.ENTITY, entity);
            param.put(Constants.WRAPPER, function.apply(entity));
            sqlSession.delete(sqlStatement, param);
        });
    }

    public <T, U>  boolean saveOrUpdateBatchByColumn2(Collection<T> entityList, Function<T, Wrapper<U>> fun) {
        T next = entityList.iterator().next();
        System.out.println(fun);
//        String fieldName = BeanUtils.getFieldName( fun);
//        System.out.println(fieldName);
        Wrapper<U> apply = fun.apply(next);
        String sqlSelect = apply.getSqlSegment();
        System.out.println(apply);
        return false;
    }

    public <T, U>  boolean saveOrUpdateBatchByColumn(Collection<T> entityList, Function<T, Wrapper<U>> function) {
        return SqlHelper.saveOrUpdateBatch(this.getSqlSessionFactory(), this.getMapperClass(), this.log, entityList, DEFAULT_BATCH_SIZE, (sqlSession, entity) -> {
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

    public boolean saveOrUpdateBatch(Collection<T> entityList, int paramSize) {
        List<List<T>> lists = ConvertUtils.batchList(entityList, paramSize);
        return executeBatch(lists, (sqlSession, list) -> {
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.LIST, list);
            String sqlStatement = this.baseMapper.getClass().getName() + "." + "insertOrUpdateBatch";
            sqlSession.insert(sqlStatement, param);
        });

    }

    public boolean saveOrUpdateBatch(Collection<T> entityList) {
        return this.saveOrUpdateBatch(entityList, PARAM_SIZE);
    }

}
