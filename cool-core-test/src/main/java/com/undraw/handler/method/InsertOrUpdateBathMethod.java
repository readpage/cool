package com.undraw.handler.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 批量插入更新
 */
public abstract class InsertOrUpdateBathMethod extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final SqlSource sqlSource = prepareSqlSource(tableInfo, modelClass);
        // 第三个参数必须和RootMapper的自定义方法名一致
        return this.addInsertMappedStatement(mapperClass, modelClass, prepareInsertOrUpdateBathName(), sqlSource, new NoKeyGenerator(), null, null);
    }
 
    protected abstract SqlSource prepareSqlSource(TableInfo tableInfo, Class<?> modelClass);
    
    protected abstract String prepareInsertOrUpdateBathName();
 
}