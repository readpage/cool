package cn.undraw.util.servlet;

import cn.undraw.util.StrUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author readpage
 * @date 2024-10-18 20:39
 */
public class UserAgent {

    private String userAgent;

    public UserAgent() {
        HttpServletRequest request = ServletUtils.getRequest();
        userAgent = request.getHeader("User-Agent");
    }

    public UserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public UserAgent(HttpServletRequest request) {
        userAgent = request.getHeader("User-Agent");
    }

    private boolean isMobile() {
        // (?i) 不区分大小写
        String regex = "(?i)android|ip(ad|hone)";
        Matcher matcher = StrUtils.matcher(regex, userAgent);
        return matcher.find();
    }

    public String getBrowser() {
        if (StrUtils.isEmpty(userAgent)) {
            return "Unknown";
        }
        Map<String, String> map = new HashMap<>();
        // (?i) 不区分大小写
        Matcher matcher = StrUtils.matcher("(?i)(edg|firefox|QQBrowser|MiuiBrowser|MQQBrowser|MicroMessenger|" +
                "Quark|DingTalk)/(\\d+.\\d+)", userAgent);
        if (matcher.find()) {
            if (userAgent.contains("Edg")) {
                return "Edge " + matcher.group(2);
            }
            return matcher.group(1) + " "  + matcher.group(2);
        }
        String regex = "(?i)(chrome|safari)/(\\d+.\\d+)";
        matcher = StrUtils.matcher(regex, userAgent);
        if (matcher.find()) {
            String[] s = userAgent.split(" ");
            if (s != null && s.length > 2) {
                if (s[s.length-2].contains("Chrome") && s[s.length-1].contains("Safari")) {
                    return "Google Chrome" + " " + matcher.group(2);
                }
            }
            return matcher.group(1) + " " + matcher.group(2);
        }
        return "Unknown";
    }

    public String getOS() {
        if (StrUtils.isEmpty(userAgent)) {
            return "Unknown";
        }
        String regex = "(?i)windows|mac|android|ip(ad|hone|ios)";
        Matcher matcher = StrUtils.matcher(regex, userAgent);
        if (matcher.find()) {
            String str = userAgent.toLowerCase();
            if (str.contains("windows nt ")) {
                Matcher matcher2 = StrUtils.matcher("Windows NT (\\d+)", userAgent);
                if (matcher2.find()) {
                    return "Windows " + matcher2.group(1);
                }
            } else if (str.contains("android")) {
                Matcher matcher2  = StrUtils.matcher(" (\\S+) Build", userAgent);
                if (matcher2.find()) {
                    return matcher2.group(1);
                }
            }
            return matcher.group();
        }
        return "Unknown";
    }


    public String getDevice() {
        return getBrowser() + " | " + getOS();
    }

    @Override
    public String toString() {
        return userAgent;
    }
}
