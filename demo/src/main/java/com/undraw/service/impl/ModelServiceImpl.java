package com.undraw.service.impl;

import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.model.Compare;
import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DateUtils;
import cn.undraw.util.MapUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.bean.BeanUtils;
import com.github.pagehelper.PageHelper;
import com.undraw.mapper.provider.Model;
import com.undraw.mapper.provider.ModelMapper;
import com.undraw.mapper.provider.ModelProvider;
import com.undraw.mapper.provider.Query;
import com.undraw.service.ModelService;
import com.undraw.util.excel.ExcelUtils;
import com.undraw.util.excel.converter.LocalDateConverter;
import com.undraw.util.excel.converter.LocalDateTimeConverter;
import com.undraw.util.page.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

@Service
public class ModelServiceImpl implements ModelService {

    @Resource
    private ModelMapper modelMapper;

    @Resource
    private ModelProvider modelProvider;

    @Override
    public boolean save(Map<String, Object> map) {
        Model model = convert(map);
        if (model == null) {
            return false;
        }
        valid(model);
        boolean b = modelMapper.insert(model) > 0;
        map.put("id", model.getId());
        return b;
    }

    @Override
    public boolean saveBatch(Map<String, Object> map) {
        Model model = convert(map);
        if (model == null) {
            return false;
        }
        valid(model);
        List<List> lists = ConvertUtils.batchList(model.getList(), model.getBatchSize());
        for (List list : lists) {
            model.setList(list);
            modelMapper.insertBatch(model);
        }

        return true;
    }

    @Override
    public boolean update(Map<String, Object> map) {
        Model model = convert(map);
        valid(model);

        List<Map> list = new ArrayList<>();
        list.add(model.getData());
        model.setList(list);
        return modelMapper.updateBatch(model) > 0;
    }

    @Override
    public boolean updateBatch(Map<String, Object> map) {
        Model model = convert(map);
        if (model == null) {
            return false;
        }
        valid(model);

        List<List> lists = ConvertUtils.batchList(model.getList(), model.getBatchSize());
        for (List list : lists) {
            model.setList(list);
            modelMapper.updateBatch(model);
        }

        return true;
    }

    @Override
    public boolean saveOrUpdateBatch(Map<String, Object> map) {
        return saveOrUpdateBatch(map, null);
    }

    @Override
    public boolean saveOrUpdateBatch(Map<String, Object> map, BiConsumer<List<Map<String, Object>>, List<Map<String, Object>>> con) {
        Model model = convert(map);
        if (model == null) {
            return false;
        }
        valid(model);

        List<List<Map>> lists = ConvertUtils.batchList(model.getList(), model.getBatchSize());
        for (List<Map> list : lists) {
            model.setList(list);
            List<String> keys = StrUtils.toList(model.getKey(), ",");
            List<LinkedHashMap<String, Object>> srcList = modelMapper.listByKeys(model);

            List saveList = new ArrayList();
            List updateList = new ArrayList();
            if (StrUtils.isEmpty(srcList)) {
                if (con != null) {
                    con.accept(model.getList(), updateList);
                }
                modelMapper.insertBatch(model);
                return true;
            }

            for (Map v1 : list) {
                Boolean b = false;
                for (Map v2 : srcList) {
                    b = null;
                    for (String key : keys) {
                        Object o1 = v1.get(key);
                        Object o2 = v2.get(key);
                        if (!Objects.equals(o1, o2)) {
                            b = false;
                            break;
                        }
                    }
                    if (b == null) {
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

            if (con != null) {
                con.accept(saveList, updateList);
            }

            if (StrUtils.isNotEmpty(saveList)) {
                model.setList(saveList);
                modelMapper.insertBatch(model);
            }
            if (StrUtils.isNotEmpty(updateList)) {
                model.setList(updateList);
                modelMapper.updateBatch(model);
            }
        }

        return true;
    }

    @Override
    public boolean remove(Map<String, Object> map) {
        Model model = convert(map);
        if (model == null) {
            return false;
        }
        boolean b = modelMapper.delete(model) > 0;
        return false;
    }

    @Override
    public boolean removeBatch(Map<String, Object> map) {
        Model model = convert(map);
        if (model == null) {
            return false;
        }
        List<List> lists = ConvertUtils.batchList(model.getList(), model.getBatchSize());
        for (List list : lists) {
            model.setList(list);
            modelMapper.deleteBatch(model);
        }
        return true;
    }

    @Override
    public List<LinkedHashMap<String, Object>> listByKey(Map<String, Object> map) {
        List newList = new ArrayList();
        Model model = convert(map);
        List<List> lists = ConvertUtils.batchList(model.getList(), model.getBatchSize());
        for (List list : lists) {
            model.setList(list);
            List<LinkedHashMap<String, Object>> list1 = modelMapper.listByKey(model);
            if (StrUtils.isNotEmpty(list1)) {
                newList.addAll(list1);
            }
        }

        return newList;
    }

    @Override
    public List<LinkedHashMap<String, Object>> listByKeys(Map<String, Object> map) {
        List newList = new ArrayList();
        Model model = convert(map);
        List<List> lists = ConvertUtils.batchList(model.getList(), model.getBatchSize());
        for (List list : lists) {
            model.setList(list);
            List<LinkedHashMap<String, Object>> list1 = modelMapper.listByKeys(model);
            if (StrUtils.isNotEmpty(list1)) {
                newList.addAll(list1);
            }
        }

        return newList;
    }


    @Override
    public List<LinkedHashMap<String, Object>> list(Map<String, Object> map) {
        Model model = convert(map);
        List<LinkedHashMap<String, Object>> list = modelMapper.list(model);
        return list;
    }

    @Override
    public PageInfo<LinkedHashMap<String, Object>> page(Map<String, Object> map) {
        Integer current = StrUtils.isNull(ConvertUtils.toInteger(map.get("current")), 1);
        Integer size = StrUtils.isNull(ConvertUtils.toInteger(map.get("size")), 5);
        Model model = convert(map);
        PageHelper.startPage(current, size);
        List<LinkedHashMap<String, Object>> list = modelMapper.list(model);
        return new PageInfo<>(list);
    }

    @Override
    public boolean upload(String tableName, MultipartFile file) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> columnList = modelMapper.columnList(tableName);
        Map<String, Map<String, String>> optionsMap = this.optionsMap(columnList, OptionsMap.label);
        try {
            FastExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, Object>>() {
                //用于存储表头的信息
                Map<Integer, String> headMap = new HashMap<>();

                @Override
                public void invoke(Map<Integer, Object> objectMap, AnalysisContext analysisContext) {
                    HashMap<String, Object> map = new HashMap<>();
                    for (int i = 0; i < objectMap.size(); i++) {
                        String s = headMap.get(i);
                        Object o = objectMap.get(i);
                        Map<String, String> m1 = optionsMap.get(s);
                        String v = String.valueOf(o);
                        if (m1 != null) {
                            v = StrUtils.isNull(m1.get(v), v);
                        }

                        //将表头作为map的key，每行每个单元格的数据作为map的value
                        map.put(s, v);
                    }
                    list.add(map);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {

                }
                //读取excel表头信息
                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    super.invokeHeadMap(headMap, context);
                    Map<String, String> columnMap = propertyMap(columnList);
                    for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                        String value = entry.getValue();
                        String s = columnMap.get(value);
                        if (s != null) {
                            entry.setValue(s);
                        }
                    }
                    this.headMap = headMap;
                }
            }).sheet().registerConverter(new LocalDateConverter()).registerConverter(new LocalDateTimeConverter()).doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map map = new HashMap();
        map.put("tableName", tableName);
        map.put("list", list);
        map.put("key", "username");
        return this.saveOrUpdateBatch(map);
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
        return optionsMap(columnList, OptionsMap.all);
    }

    private enum OptionsMap {
        all, label, value
    }

    private Map<String, Map<String, String>> optionsMap(List<Map<String, Object>> columnList, OptionsMap optionsMap) {
        Map<String, Map<String, String>> map = new HashMap<>();
        for (Map<String, Object> column : columnList) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            List<String> remarkList = StrUtils.toList((String) column.get("COLUMN_COMMENT"), ",");
            if (remarkList.size() > 1) {
                String[] options = remarkList.get(1).split("\\.");
                if (options.length > 1) {
                    List<LinkedHashMap<String, Object>> optionsList = this.list(MapUtils.of("tableName", options[0], "module", options[1]));
                    Map<String, String> m2 = new HashMap<>();
                    for (LinkedHashMap<String, Object> m1 : optionsList) {
                        String label = String.valueOf(m1.get("label"));
                        String value = String.valueOf(m1.get("value"));
                        switch (optionsMap.name()) {
                            case "all":
                                m2.put(label, value);
                                m2.put(value, label);
                                break;
                            case "label":
                                m2.put(label, value);
                                break;
                            case "value":
                                m2.put(value, label);
                                break;
                        }


                    }
                    map.put(property, m2);
                }
            }
        }
        return map;
    }

    @Override
    public void export(HttpServletResponse response, Map<String, Object> map) {
        String tableName = String.valueOf(map.get("tableName"));
        Map<String, Object> table = modelMapper.table(tableName);
        String tableTitle = StrUtils.isNull(String.valueOf(table.get("TABLE_COMMENT")), tableName);
        List<Map<String, Object>> columnList = getColumn(tableName, map);
        List<LinkedHashMap<String, Object>> dataList = this.list(map);
        List<List<String>> head = new ArrayList<>();

        Map<String, Map<String, String>> optionsMap = this.optionsMap(columnList, OptionsMap.value);
        for (Map<String, Object> column : columnList) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            String remark = StrUtils.isNull(String.valueOf(column.get("COLUMN_COMMENT")), property);
            head.add(Arrays.asList(remark.split(",")[0]));
        }

        for (LinkedHashMap<String, Object> data : dataList) {
            for (Map.Entry<String, Map<String, String>> entry : optionsMap.entrySet()) {
                String property = entry.getKey();
                Map<String, String> m1 = entry.getValue();
                String v1 = String.valueOf(data.get(property));
                String v2 = m1.get(v1);
                if (StrUtils.isNotEmpty(v2)) {
                    data.put(property, m1.get(v1));
                }
            }
        }

        ExcelUtils.export(response, tableTitle, "sheet", dataList, head);
    }

    @Override
    public String where(Query query) {
        return modelProvider.where(query);
    }

    @Override
    public void where(Map<String, Object> map, Class<?> objClass) {
        modelProvider.where(new Query(BeanUtils.getFieldNames(objClass), map));
    }

    public TreeSet<String> getColumnNames(List<String> tableNames) {
        TreeSet<String> columnNames = new TreeSet<>();
        new TreeSet<>(tableNames).forEach(tableName -> {
            List<Map<String, Object>> columnList = modelMapper.columnList(tableName);
            for (Map<String, Object> column : columnList) {
                String columnName = (String) column.get("COLUMN_NAME");
                columnNames.add(columnName);
            }
        });
        return columnNames;
    }

    @Override
    public boolean sync(Compare compare, String tableName, String key) {
        Map map = new HashMap();
        map.put("tableName", tableName);
        map.put("list", compare.saveList);
        if (StrUtils.isNotEmpty(compare.saveList)) {
            this.saveBatch(map);
        }

        Map map2 = new HashMap();
        map2.put("tableName", tableName);
        map2.put("list", compare.updateList);
        map2.put("key", key);
        if (StrUtils.isNotEmpty(compare.updateList)) {
            this.updateBatch(map2);
        }

        Map map3 = new HashMap();
        map3.put("tableName", tableName);
        map3.put("list", compare.removeList);
        map3.put("key", key);
        if (StrUtils.isNotEmpty(compare.removeList)) {
            this.removeBatch(map3);
        }
        return true;
    }

    @Override
    public boolean sync(Compare compare, String tableName) {
        return sync(compare, tableName, "id");
    }

    private List<Map<String, Object>> getColumn(String tableName, Map<String, Object> map) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> columnList = modelMapper.columnList(tableName);
        List<String> fieldList = new ArrayList<>();
        Object o = map.get("column");
        if (o != null) {
           fieldList = StrUtils.toList(String.valueOf(o), ",");
        }
        for (Map<String, Object> column : columnList) {
            String columnName = (String) column.get("COLUMN_NAME");
            String property = StrUtils.toCamelCase(columnName);
            if (fieldList.size() == 0) {
                list.add(column);
            } else {
                if (fieldList.contains(property)) {
                    list.add(column);
                }
            }

        }
        return list;
    }

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

    private void valid(Model model) {
        List<Map<String, Object>> columns = model.getColumns();
        List list = model.getList();
        if (StrUtils.isEmpty(list)) {
            list = new ArrayList<>(Arrays.asList(model.getData()));
        }
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map)) {
                list.set(i, BeanUtils.objectToMap(list.get(i)));
            }
            Map<String, Object> m = (Map<String, Object>) list.get(i);
            for (Map<String, Object> column : columns) {
                String columnName = (String)column.get("COLUMN_NAME");
                String property = StrUtils.toCamelCase(columnName);
                String extra = (String) column.get("EXTRA");
                String nullable = (String) column.get("IS_NULLABLE");

                String title = property;
                String remark = (String) column.get("COLUMN_COMMENT");
                String[] remarks = remark.split(",");
                if (remarks.length > 0) {
                    String a = remarks[0];
                    if (StrUtils.isNotEmpty(a)) {
                        title = a;
                    }
                }

                // 不能为空
                if (!Objects.equals(extra, "auto_increment") && Objects.equals(nullable, "NO")) {
                    Object o = m.get(property);
                    if (StrUtils.isEmpty(o)) {
                        String s = String.format("%s为必填项不能为空", title);
                        if (list.size() > 0) {
                            s = String.format("第%d行%s", i + 1, s);
                        }
                        throw new CustomerException(s);
                    }
                }

                if (m.get(property) != null) {
                    String type = (String) column.get("COLUMN_TYPE");
                    switch (type) {
                        case "date":
                            m.put(property, DateUtils.toLocalDate(String.valueOf(m.get(property))));
                            break;
                        case "datetime":
                            m.put(property, DateUtils.toDateTime(String.valueOf(m.get(property))));
                            break;
                        case "int":
                            if (!StrUtils.isNumber(m.get(property))) {
                                String s = String.format("%s不是数字", title);
                                if (list.size() > 0) {
                                    s = String.format("第%d行%s", i + 1, s);
                                }
                                throw new CustomerException(s);
                            }
                            break;
                    }
                }
            }
        }
    }
}
