package com.undraw.basic;

import cn.undraw.util.filter.JsonFilterUtils;
import cn.undraw.util.filter.SensitiveUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author readpage
 * @date 2023-04-03 22:15
 */
public class SensitiveTest {
    private static List<String> list = new ArrayList<>();

    @Test
    public void test() {
        System.out.println(SensitiveUtils.filter("李明你好!"));
        SensitiveUtils.add("李明");
        System.out.println(SensitiveUtils.filter("李明你好!"));
        SensitiveUtils.remove("李明");
        System.out.println(SensitiveUtils.filter("李明你好!"));
    }

    @Test
    public void test2() {
        String str = SensitiveUtils.xor("李明");
        System.out.println(str);
    }


    @Test
    public void test3() {
//        String json = "[{\"name\":\"John\", \"password\":123, {\"password\":123}, \"age\":30, \"city\":\"New York\"},{\"name\":\"John\", \"password\":123, {\"password\":123}, \"age\":30, \"city\":\"New York\"}]";
        String json = "{\"id\":1,\"username\":\"root\",\"nickname\":\"root\",\"password\":\"\",\"status\":true,\"avatar\":\"http://dummyimage.com/100x100\",\"email\":\"root@qq.com\",\"phone\":\"13715463087\",\"createTime\":null,\"updateTime\":null,\"auths\":[\"root\"]}";
        String filter = JsonFilterUtils.filter(json, (k, v) -> {
            if ("id".equals(k)) {
//                v = v.substring(0, 2) + "*****" + v.substring(v.length()-2, v.length());
            }
            if ("openid".equals(k)) {
                v = "***********";
            }
            return v;
        });
        System.out.println(filter);
    }

}
