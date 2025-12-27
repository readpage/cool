package com.undraw.mapper.provider;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Model {

    private Long id;

    private Map<String, Object> table;

    private List<Map<String, Object>> columns;

    private Map<String, Object> data;

    private List list;

    private String key = "id";

    private Integer batchSize = 1000;

    public Model() {
    }

    public Model(Map<String, Object> data, Map<String, Object> table, List<Map<String, Object>> columns) {
        this.data = data;
        this.table = table;
        this.columns = columns;
    }
}
