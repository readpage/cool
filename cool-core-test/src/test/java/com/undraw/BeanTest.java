package com.undraw;

import cn.undraw.util.bean.BeanUtils;
import com.undraw.domain.entity.Role;
import org.junit.jupiter.api.Test;

/**
 * @author readpage
 * @date 2025-02-15 11:07
 */
public class BeanTest {

    @Test
    public void test() {
        System.out.println(BeanUtils.getFieldName(Role::getNickname, Role::getName, Role::getCreateTime));
    }
}
