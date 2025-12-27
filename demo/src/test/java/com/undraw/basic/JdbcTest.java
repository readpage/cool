package com.undraw.basic;

import com.undraw.domain.entity.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class JdbcTest {

    @Resource
    private JdbcTemplate jdbcTemplate;


    /**
     * 需要 Java Bean 元素类型时，使用 {@link BeanPropertyRowMapper}，
     * 需要 Map 类型时，使用 {@link ColumnMapRowMapper}
     */
    @Test
    public void list() {
        Class clazz = User.class;
        Object[] args = {23};
        String sql = "SELECT id, username, password, create_time, update_time FROM user WHERE id = ?";
        List<User> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(clazz), args);
        System.out.println(list);
    }

    @Test
    public void insert() {
        String sql = "INSERT user (username, password) VALUES (?, ?)";
        List list = Arrays.asList("test2", "test2");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < list.size(); i++) {
                ps.setObject(i + 1, list.get(i));
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        System.out.println(key);
    }

    @Test
    public void insertBatch() {
        String sql = "INSERT user (username, password) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        batchArgs.add(new Object[]{"test2", "test2"});
        batchArgs.add(new Object[]{"test3", "test3"});
        int[] ints = jdbcTemplate.batchUpdate(sql, batchArgs);
        System.out.println(ints.length);
    }

    @Test
    public void updateBatch() {
        String sql = "UPDATE user SET username = ? WHERE id = ?";
        List<Object[]> batchArgs = new ArrayList<>();
        batchArgs.add(new Object[]{"test22", 1009});
        batchArgs.add(new Object[]{"test33", 1010});
        int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchArgs);
        System.out.println("Updates executed: " + updateCounts.length);

    }

    @Test
    public void deleteBatch() {
        String sql = "DELETE FROM user WHERE id = ?";
        List<Object[]> batchArgs = new ArrayList<>();
        batchArgs.add(new Object[]{1009});
        batchArgs.add(new Object[]{1010});
        int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchArgs);
        System.out.println("Updates executed: " + updateCounts.length);

    }


}
