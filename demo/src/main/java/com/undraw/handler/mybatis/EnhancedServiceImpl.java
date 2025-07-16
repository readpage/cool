package com.undraw.handler.mybatis;

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

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class EnhancedServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {


    public List<T> listByKey(Collection<T> entityList, SFunction<T, ?>...fun) {
        String fieldName = BeanUtils.getFieldName(fun);
        List<String> fieldNames = StrUtils.toList(fieldName, ",");
        return listByKey(entityList, fieldNames);
    }

    public List<T> listByKey(Collection<T> entityList, List<String> fieldNames) {
        List list = new ArrayList();
        if (entityList != null && entityList.size() > 0) {
            StringBuilder f = new StringBuilder("");
            fieldNames.stream().forEach(s -> {
                f.append(StrUtils.toUnderScoreCase(s)).append(",");
            });
            f.deleteCharAt(f.length() - 1);
            String fieldName = f.toString();

            List<List<T>> lists = ConvertUtils.batchList(entityList, 200 / fieldNames.size());
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

    public <T, U>  boolean saveOrUpdateBatchByColumn(Collection<T> entityList, Function<T, Wrapper<U>> fn) {
        boolean success = true;
        if (entityList != null && entityList.size() > 0) {
            T next = entityList.iterator().next();
            List<String> fieldNames = getFieldNames(next, fn);
            List<List<T>> lists = ConvertUtils.batchList(entityList, DEFAULT_BATCH_SIZE / fieldNames.size());
            for (List<T> list : lists) {
                List<T> saveList = new ArrayList();
                List<T> updateList = new ArrayList();
                List srcList = this.listByKey((Collection) list, fieldNames);
                for (T v1 : entityList) {
                    boolean b = false;
                    for (Object v2 : srcList) {
                        b = BeanUtils.equals(v1, v2, fieldNames);
                        if (b) {
                            b = true;
                            break;
                        }
                    }
                    if (b) {
                        updateList.add(v1);
                    } else {
                        saveList.add(v1);
                    }
                }
                if (StrUtils.isNotEmpty(saveList)) {
                    success &= this.saveBatch((Collection) saveList);
                }
                if (StrUtils.isNotEmpty(updateList)) {
                    success &= this.updateBatchByColumn((Collection) updateList, fn);
                }
            }
        }
        return success;
    }

    private <T, U> List<String> getFieldNames(T o, Function<T, Wrapper<U>> fn) {
        Wrapper<U> apply = fn.apply(o);
        String sqlSelect = apply.getSqlSegment();
        List<Field> fields = BeanUtils.getFields(o.getClass());
        List<String> fieldNames = new ArrayList();
        for (Field field : fields) {
            String underScoreCase = StrUtils.toUnderScoreCase(field.getName());
            boolean contains = sqlSelect.contains(underScoreCase);
            if (contains) {
                fieldNames.add(field.getName());
            }
        }
        return fieldNames;
    }

    @Deprecated
    public <T, U> boolean saveOrUpdateBatchByColumn2(Collection<T> entityList, Function<T, Wrapper<U>> function) {
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
