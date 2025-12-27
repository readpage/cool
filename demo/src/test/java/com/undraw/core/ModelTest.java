package com.undraw.core;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.MapUtils;
import cn.undraw.util.StrUtils;
import com.undraw.domain.entity.User;
import com.undraw.mapper.provider.Model;
import com.undraw.mapper.provider.ModelMapper;
import com.undraw.service.ModelService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class ModelTest {

    // https://codeleading.com/article/37856496929/

    @Resource
    private ModelService modelService;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;


    private Model convert(Map<String, Object> map) {
        String tableName = "";
        Model model = new Model();
        model.setData(new HashMap<>());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            switch (key) {
                case "tableName":
                    tableName = (String) value;
                    break;
                case "list":
                    model.setList((List)value);
                    break;
                case "key":
                    model.setKey((String)value);
                    break;
                case "batchSize":
                    model.setBatchSize((Integer)value);
                    break;
                default:
                    model.getData().put(key, value);
            }
        }
        Map<String, Object> table = modelMapper.table(tableName);
        if (table == null) {
            throw new CustomerException(tableName + "表不存在");
        }
        model.setTable(table);
        model.setColumns(modelMapper.columnList(tableName));
        return model;
    }

    @Test
    public void insert() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "test");
        map.put("sex", null);
        map.put("age", 20);
        map.put("tableName", "model");
        boolean save = modelService.save(map);
        System.out.println(save);
    }

    @Test
    public void saveBatch() {
        List list = new ArrayList();
        Map<String, Object> map = new HashMap<>();
        map.put("username", "test3");
        map.put("sex", null);
        map.put("age", 20);
        list.add(map);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("username", "test4");
        map2.put("sex", null);
        map2.put("age", 24);
        list.add(map2);
        boolean save = modelService.saveBatch(Map.of("tableName", "model", "list", list));
        System.out.println(save);
    }

    @Test
    public void update() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "30");
        map.put("username", "test");
        map.put("sex", null);
        map.put("age", 20);
        map.put("tableName", "model");
        boolean update = modelService.update(map);
        System.out.println(update);
    }

    @Test
    public void updateBatch() {
        List list = new ArrayList();
        Map map = new HashMap();
        map.put("tableName", "model");
        map.put("key", "username");
        Map<String, Object> map1 = new HashMap<>();
        map1.put("username", "test3");
        map1.put("sex", 1);
        map1.put("age", 20);
        list.add(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("username", "test4");
        map2.put("sex", 1);
        map2.put("age", 24);
        list.add(map2);
        map.put("list", list);
        boolean update = modelService.updateBatch(map);
        System.out.println(update);
    }

    @Test
    public void remove() {
        Map map = new HashMap();
        map.put("tableName", "model");
        map.put("list", Arrays.asList(34, 35));
        boolean remove = modelService.remove(map);
        System.out.println(remove);
    }

    @Test
    public void removeBatch() {
        List list = new ArrayList();
        Map map = new HashMap();
        map.put("tableName", "model");
        map.put("key", "username,age");
        list.add(Map.of("username", "test", "age", 21));
        map.put("list", list);
        boolean remove = modelService.removeBatch(map);
        System.out.println(remove);
    }


    @Resource
    private ModelMapper modelMapper;

    @Test
    public void saveOrUpdateBatch() {
        String tableName = "model";
        List<Map<String, Object>> columnList = modelMapper.columnList(tableName);
        Map<String, Map<String, String>> map = optionsMap(columnList);
        System.out.println(map);
    }

    private Map<String, String> propertyMap(List<Map<String, Object>> columnList) {
        Map<String, String> map = new HashMap<>();
        for (Map<String, Object> column : columnList) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            String title = StrUtils.isNull(((String) column.get("COLUMN_COMMENT")).split(",")[0], property);
            map.put(title, property);
        }
        return map;
    }

    private Map<String, Map<String, String>> optionsMap(List<Map<String, Object>> columnList) {
        Map<String, Map<String, String>> map = new HashMap<>();
        for (Map<String, Object> column : columnList) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            List<String> remarkList = StrUtils.toList((String) column.get("COLUMN_COMMENT"), ",");
            if (remarkList.size() > 1) {
                String[] options = remarkList.get(1).split("\\.");
                if (options.length > 1) {
                    List<LinkedHashMap<String, Object>> optionsList = modelService.list(MapUtils.of("tableName", options[0], "module", options[1]));
                    Map<String, String> m2 = new HashMap<>();
                    for (LinkedHashMap<String, Object> m1 : optionsList) {
                        String label = String.valueOf(m1.get("label"));
                        String value = String.valueOf(m1.get("value"));
                        m2.put(label, value);
                        m2.put(value, label);
                    }
                    map.put(property, m2);
                }
            }
        }
        return map;
    }

    @Test
    public void select() {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", "model");
        map.put("usernameLike", "test");
        map.put("createTime", new String[]{"2025-01-01", "2025-08-01"});
        List<LinkedHashMap<String, Object>> list = modelService.list(map);
        System.out.println(list);
    }

    @Test
    public void list() {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", "model");
        map.put("usernameLike", "test");
        System.out.println(modelService.list(map));
    }

    @Test
    public void listByKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", "model");
        map.put("key", "username,age");
        Map map1 = MapUtils.of("username", "test1", "age", 20);

        map.put("list", List.of(map1));
        List<LinkedHashMap<String, Object>> list = modelService.listByKeys(map);
        System.out.println(list);
    }

    @Test
    public void listByKey2() {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", "model");
        map.put("key", "username");
        map.put("list", List.of("test1"));
        List<LinkedHashMap<String, Object>> list = modelService.listByKey(map);
        System.out.println(list);
    }

    @Test
    public void query() {
        Map<String, Object> map = new HashMap<>();
        map.put("usernameLike", "test");
        modelService.where(map, User.class);
        System.out.println(map);
    }


    @Test
    public void objList() {
    }

    @Test
    public void mapList() {
    }

}
