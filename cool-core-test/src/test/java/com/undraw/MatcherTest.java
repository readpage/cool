package com.undraw;

import cn.undraw.util.StrUtils;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherTest {
    @Test
    public void test() {
        String str = "Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry '86' for key 'user.PRIMARY'\n" +
                "; Duplicate entry '86' for key 'user.PRIMARY'; ";
        Pattern pattern = Pattern.compile("Duplicate entry '(.+?)' for key");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            System.out.println(matcher.group());
            String content = matcher.group(1);
            System.out.println("匹配到的内容是: " + content);
        } else {
            System.out.println("没有找到匹配的内容");
        }
    }


    public String getBrowser(String v) {
        if (isMobile(v)) {
            return "mobile";
        } else {
            String regex = "(?i)(chrome|firefox|safari)/(\\d+)";
            Matcher matcher = StrUtils.matcher(regex, v);
            if (matcher.find()) {
                return matcher.group(1) + " " + matcher.group(2);
            }
        }
        return "Unknown";
    }

    private boolean isMobile(String v) {
        String regex = "(?i)android|ip(ad|hone)";
        Matcher matcher = StrUtils.matcher(regex, v);
        return matcher.find();
    }

    public String getDevice(String v) {
        StringBuffer sb = new StringBuffer();
        String regex = "(?i)android|ip(ad|hone)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(v);
        if (matcher.find()) {
            System.out.println("Device: " + matcher.group());
        } else {
            sb.append("Unknown");
        }
        return sb.toString();
    }

    @Test
    public void test2() {
        String edge = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36 Edg/129.0.0.0";
        String googleChrome = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36";
        String firefox = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0";

        // (?i) 修饰符进行不区分大小写的匹配
        Pattern pattern = Pattern.compile("(?i)(firefox|chrome|safari|opera|msie|trident)/(\\d+)");
        Matcher matcher = pattern.matcher(firefox);

        if (matcher.find()) {
            System.out.println(matcher.group());
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println("匹配到的内容是: " + matcher);
        } else {
            System.out.println("没有找到匹配的内容");
        }


        // 定义正则表达式来识别设备型号
        Pattern DEVICE_PATTERN = Pattern.compile("(?i)(windows|mac|linux|mobile|android|ip(ad|hone))");
        // 识别设备型号
        Matcher deviceMatcher = DEVICE_PATTERN.matcher(firefox);
        if (deviceMatcher.find()) {
            System.out.println("Device: " + deviceMatcher.group());
        } else {
            System.out.println("Device not found");
        }
    }

    @Test
    public void test3() {
        // device
        String browser = "";
        String os = "";
        String wechat = "Mozilla/5.0 (Linux; Android 12; 22041216UC Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/126.0.6478.188 Mobile Safari/537.36 XWEB/1260117 MMWEBSDK/20240801 MMWEBID/7949 MicroMessenger/8.0.51.2720(0x28003339) WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64";
        String edge = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36 Edg/129.0.0.0";

        String patternString = "(?:(Windows NT 10\\.0)|(Windows NT 11\\.0)|(Mac OS X)|(X11; Linux))";
        Pattern pattern = Pattern.compile("(?i)(windows|mac|linux|mobile|android|ip(ad|hone)) \\d+");
        Matcher matcher = pattern.matcher(wechat);
        if (matcher.find()) {
            System.out.println("Device: " + matcher.group());
        }
    }

    @Test
    public void test4() {
        String v = "Mozilla/5.0 (Linux; U; Android 12; zh-CN; 22041216UC Build/SP1A.210812.016) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/100.0.4896.58 Quark/7.4.0.670 Mobile Safari/537.36";
        boolean mobile = isMobile(v);
        System.out.println(mobile);
    }

}
