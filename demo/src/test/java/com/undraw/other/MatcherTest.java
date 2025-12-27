package com.undraw.other;

import cn.undraw.util.StrUtils;
import cn.undraw.util.servlet.UserAgent;
import org.junit.jupiter.api.Test;

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

    @Test
    public void test2() {
        String edge = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36 Edg/129.0.0.0";
        String googleChrome = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36";
        String firefox = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0";
        String qq = "Mozilla/5.0 (Linux; Android 12; 22041216UC Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/109.0.5414.86 MQQBrowser/6.2 TBS/046613 Mobile Safari/537.36 V1_AND_SQ_9.0.95_7368_YYB_D QQ/9.0.95.19320 NetType/WIFI WebP/0.3.0 AppId/537242080 Pixel/1080 StatusBarHeight/100 SimpleUISwitch/0 QQTheme/1000 StudyMode/0 CurrentMode/0 CurrentFontScale/1.0 GlobalDensityScale/0.9818182 AllowLandscape/false InMagicWin/0";
        String wechat = "Mozilla/5.0 (Linux; Android 12; 22041216UC Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/126.0.6478.188 Mobile Safari/537.36 XWEB/1260117 MMWEBSDK/20240801 MMWEBID/7949 MicroMessenger/8.0.51.2720(0x28003339) WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64";
        String qiYeWechat = "Mozilla/5.0 (Linux; Android 12; 22041216UC Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/109.0.5414.86 MQQBrowser/6.2 TBS/047001 Mobile Safari/537.36 wxwork/4.1.28 MicroMessenger/7.0.1 NetType/WIFI Language/zh Lang/zh ColorScheme/Light";
        String quark = "Mozilla/5.0 (Linux; U; Android 12; zh-CN; 22041216UC Build/SP1A.210812.016) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/100.0.4896.58 Quark/7.4.0.670 Mobile Safari/537.36";
        String iphone = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
//        String browser = getBrowser(quark);
//        System.out.println(browser);
//        System.out.println(getOS(edge));
        UserAgent userAgent = new UserAgent(edge);
        String device = userAgent.getDevice();
        System.out.println(device);
    }

    @Test
    public void test3() {
        String wechat = "Mozilla/5.0 (Linux; Android 12; 22041216UC Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/126.0.6478.188 Mobile Safari/537.36 XWEB/1260117 MMWEBSDK/20240801 MMWEBID/7949 MicroMessenger/8.0.51.2720(0x28003339) WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64";
        Matcher matcher = StrUtils.matcher(wechat, "test");
        if (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

}
