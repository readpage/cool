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

import java.util.List;
import java.util.Map;
import java.util.Set;

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
