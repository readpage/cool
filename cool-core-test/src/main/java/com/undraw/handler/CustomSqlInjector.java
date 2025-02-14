package com.undraw.handler;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.undraw.handler.method.InsertBatchMethod;

import java.util.List;

public class CustomSqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
 
        // 注意：此SQL注入器继承了DefaultSqlInjector(默认注入器)，调用了DefaultSqlInjector的getMethodList方法，保留了mybatis-plus的自带方法
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
        InsertBatchMethod insertBatchMethod = new InsertBatchMethod();
        methodList.add(new InsertBatchMethod());
        return methodList;
    }
}