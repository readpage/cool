package com.undraw.util.jdbc;

import lombok.Data;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Data
public class Model<T> {

    private String tableName;

    private Map<String, Object> table;

    private List<Map<String, Object>> columns;

    private Map<String, Object> data;

    private Class<T> type;

    private List list;

    private String key = "id";

    private Integer batchSize = 1000;


    public Model() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            this.type = (Class<T>) ((ParameterizedType)genericSuperclass).getActualTypeArguments()[0];
        }
    }

    public Model(String tableName, Map<String, Object> data) {
        this.tableName = tableName;
        this.data = data;
    }

}