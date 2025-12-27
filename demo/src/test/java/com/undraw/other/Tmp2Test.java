package com.undraw.other;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.bean.BeanUtils;
import com.undraw.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Map;

@SpringBootTest
public class Tmp2Test {

    @Test
    public void test() {
        throw new RuntimeException("错误");
    }

    @Test
    public void test2() {
        throw new CustomerException("警告");
    }

   @Test
    public void test3() {
       String join = String.join(",", Collections.nCopies(2, "?"));
       System.out.println(join);
       System.out.println(Collections.nCopies(2, "?"));
       User user = new User();
       user.setUsername("user");
       user.setPassword("123456");
       Map<String, Object> entry = BeanUtils.getEntry(user);
       System.out.println(entry);
   }

}
