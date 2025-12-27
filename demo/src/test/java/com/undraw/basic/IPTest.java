package com.undraw.basic;

import cn.undraw.util.servlet.IpUtils;
import org.junit.jupiter.api.Test;

public class IPTest {


    // ip白名单  gateway tokenFilter
//    public boolean ipFilter(ServerHttpRequest request, String path) {
//        Map<String, List<String>> map = new HashMap<>();
//        map.put("0:0:0:0:0:0:0:1", Arrays.asList("/**"));
//        String clientIP = getClientIP(request);
//        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//            String key = entry.getKey();
//            List<String> value = entry.getValue();
//            if (clientIP.equals(key)) {
//                for (String s : value) {
//                    if (antPathMatcher.match(s, path)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }


    @Test
    public void test() {
        System.out.println(IpUtils.isIPInRange("192.168.1.165", "192.168.1.*"));
        System.out.println(IpUtils.isIPInRange("120.133.55.5", "*.*.*.*"));
        System.out.println(IpUtils.isIPInRange("192.168.1.165", "*.133.1.*"));
    }
}
