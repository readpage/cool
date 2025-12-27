package com.undraw.other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
public class KeyValueListManager {
    private Map<String, List<String>> map = new HashMap<>();
 
    // 添加或更新键对应的值
    public void addValue(String key, String value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }
 
    // 删除键对应的所有值
    public void removeAllValues(String key) {
        map.remove(key);
    }
 
    // 删除键对应的特定值
    public void removeValue(String key, String value) {
        if (map.containsKey(key)) {
            List<String> values = map.get(key);
            values.remove(value);
            if (values.isEmpty()) {
                map.remove(key); // 如果列表为空，也删除键
            }
        }
    }
 
    // 获取键对应的所有值
    public List<String> getValues(String key) {
        return map.getOrDefault(key, new ArrayList<>()); // 如果键不存在，返回空列表
    }
 
    public static void main(String[] args) {
        KeyValueListManager manager = new KeyValueListManager();
        manager.addValue("key1", "value1");
        manager.addValue("key1", "value2");
        manager.addValue("key2", "value3");
        manager.addValue("key2", "value4");
        System.out.println(manager.getValues("key1")); // 输出: [value1, value2]
        manager.removeValue("key1", "value2");
        System.out.println(manager.getValues("key1")); // 输出: [value1]
        System.out.println(manager.getValues("key2")); // 输出: [value3, value4]
        manager.removeAllValues("key2");
        System.out.println(manager.getValues("key2")); // 输出: []
    }
}