package com.undraw;

import cn.undraw.util.bean.AnnoUtils;
import cn.undraw.util.bean.BeanUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.undraw.domain.entity.User;
import org.junit.jupiter.api.Test;

/**
 * @author readpage
 * @date 2025-02-15 11:07
 */
public class BeanTest {

    @Test
    public void test() {
        System.out.println(BeanUtils.getFieldName(User::getUsername, User::getPassword, User::getPhone));
        BeanUtils.getFields(User.class).forEach(field -> {
            Object access = AnnoUtils.getAnnoValueByField(field, JsonProperty.class, "access");
            System.out.println(access);
        });
    }
}
