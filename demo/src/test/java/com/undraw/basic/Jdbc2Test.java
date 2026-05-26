package com.undraw.basic;

import cn.undraw.util.MapUtils;
import com.undraw.domain.entity.User;
import com.undraw.util.jdbc.JdbcUtil;
import com.undraw.util.jdbc.Model;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

@SpringBootTest
public class Jdbc2Test {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private JdbcUtil jdbcUtil;

    @Test
    public void table() {
        Map<String, Object> table = jdbcUtil.table("user");
        System.out.println(table);
        List<Map<String, Object>> list = jdbcUtil.columnList("user");
        System.out.println(list);
    }

    @Test
    public void list() {
        Model<User> model = new Model<>(){};
        model.setTableName("user");
        model.setData(MapUtils.of("id", 23));
        List<User> list = jdbcUtil.list(model);
        System.out.println(list);
    }

    @Test
    public void list2() {
        Model<Object> model = new Model<>();
        model.setTableName("user");
        model.setData(MapUtils.of("id", 23));
        List<Map<String, Object>> list = jdbcUtil.listMaps(model);
        System.out.println(list);
    }

    @Test
    public void listByKey() {
        Model model = new Model();
        model.setTableName("user");
        model.setKey("password,age");
        model.setList(Arrays.asList(new User("test", 22), new User("test", 23)));
        List list = jdbcUtil.listByKey(model);
        System.out.println(list);
    }

    @Test
    public void listByKey2() {
        Model model = new Model();
        model.setTableName("user");
        model.setKey("age");
        model.setList(Arrays.asList(22));
        List list = jdbcUtil.listByKey(model);
        System.out.println(list);
    }

    @Test
    public void insert() {
        Model model = new Model();
        model.setTableName("user");
        Map map = new HashMap();
        map.put("username", "test5");
        map.put("password", "test5");
        model.setData(map);
        int n = jdbcUtil.insert(model);
        System.out.println(n);
    }

    @Test
    public void insertBatch() {
        Model model = new Model();
        model.setTableName("user");
        Map map = new HashMap();
        map.put("username", "test5");
        map.put("password", "test5");
        Map map2 = new HashMap();
        map2.put("username", "test6");
        map2.put("password", "test6");
        model.setList(Arrays.asList(map, map2));
        int i = jdbcUtil.insertBatch(model);
        System.out.println(i);
    }

    @Test
    public void updateBatch() {
        Model model = new Model();
        model.setTableName("user");
        Map map = new HashMap();
        map.put("id", "1023");
        map.put("username", "test2");
        map.put("password", "test2");
        Map map2 = new HashMap();
        map2.put("id", "1024");
        map2.put("username", "test3");
        map2.put("password", "test3");
        model.setList(Arrays.asList(map, map2));
        int i = jdbcUtil.updateBatch(model);
        System.out.println(i);
    }

    @Test
    public void deleteBatch() {
        Model model = new Model();
        model.setTableName("user");
        model.setList(Arrays.asList(1025, 1026));
        int i = jdbcUtil.deleteBatch(model);
        System.out.println(i);
    }

    @Test
    public void scan() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class)); // 过滤条件，这里以Object类为例

        Set<BeanDefinition> candidates = scanner.findCandidateComponents("com.undraw.domain.entity"); // 指定要扫描的包路径
        for (BeanDefinition candidate : candidates) {
            try {
                Class<?> clazz = Class.forName(candidate.getBeanClassName());
                System.out.println(clazz);
            } catch (ClassNotFoundException e) {
            }
        }
    }

}
