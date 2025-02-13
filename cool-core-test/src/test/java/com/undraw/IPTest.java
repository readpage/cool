package com.undraw;

import cn.undraw.util.servlet.IpUtils;
import org.junit.Test;

public class IPTest {


    @Test
    public void test() {
        System.out.println(IpUtils.isIPInRange("192.168.1.165", "192.168.1.*"));
        System.out.println(IpUtils.isIPInRange("120.133.55.5", "*.*.*.*"));
        System.out.println(IpUtils.isIPInRange("192.168.1.165", "*.133.1.*"));
    }
}
