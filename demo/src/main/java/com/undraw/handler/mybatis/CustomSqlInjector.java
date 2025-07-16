package com.undraw.handler.mybatis;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.undraw.handler.mybatis.method.InsertBatchMethod;
import com.undraw.handler.mybatis.method.ListByKeyMethod;
import com.undraw.handler.mybatis.method.MysqlInsertOrUpdateMethod;
import com.undraw.handler.mybatis.method.UpdateBatchMethod;

import java.util.List;

public class CustomSqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
 
        // 注意：此SQL注入器继承了DefaultSqlInjector(默认注入器)，调用了DefaultSqlInjector的getMethodList方法，保留了mybatis-plus的自带方法
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
        methodList.add(new ListByKeyMethod("listByKey"));
        methodList.add(new InsertBatchMethod("insertBatch"));
        methodList.add(new UpdateBatchMethod("updateBatch"));
        methodList.add(new MysqlInsertOrUpdateMethod("insertOrUpdateBatch"));
        return methodList;
    }
}