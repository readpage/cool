package cn.undraw.util.servlet;

import cn.undraw.util.StrUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author readpage
 * @date 2022-11-29 22:05
 */
@Slf4j
public class IpUtils {
    private static byte[] bytes = null;

    static {
        //db
        InputStream is = null;
        try {
            is = new ClassPathResource("/assets/city/ip2region.db").getInputStream();
            if (is == null) {
                throw new RuntimeException("Invalid ip2region.db file");
            }
            bytes = readBytes(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static boolean isIp(String ip) {
        boolean b1 = ip.matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
        return b1;
    }

    public static boolean isNotIp(String ip) {
        return !isIp(ip);
    }

    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int num;
        while ((num = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, num);
        }
        byteArrayOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 获取城市信息
     * @param ip 数字ip
     * @param algorithm
     * @return 国家|区域|省份|城市|ISP
     */
    public static String getIpInfo(String ip, int... algorithm){
        if (StrUtils.isEmpty(algorithm)) {
            algorithm = new int[]{3};
        }
        try {
            //        查询算法
//        1.memory算法：整个数据库全部载入内存，单次查询都在0.1x毫秒内，C语言的客户端单次查询在0.00x毫秒级别。
//        2.binary算法：基于二分查找，基于ip2region.db文件，不需要载入内存，单次查询在0.x毫秒级别。
//        3.b-tree算法：基于btree算法，基于ip2region.db文件，不需要载入内存，单词查询在0.x毫秒级别，比binary算法更快。
//        ps：任何客户端b-tree都比binary算法快，当然memory算法固然是最快的

//        int algorithm = DbSearcher.BTREE_ALGORITHM; //B-tree
            //DbSearcher.BINARY_ALGORITHM //Binary
            //DbSearcher.MEMORY_ALGORITYM //Memory
            DbConfig config = new DbConfig();
            DbSearcher searcher = new DbSearcher(config, bytes);
            //define the method
            Method method = null;
            switch (algorithm[0])
            {
                case DbSearcher.BINARY_ALGORITHM:
                    method = searcher.getClass().getMethod("binarySearch", String.class);
                    break;
                case DbSearcher.MEMORY_ALGORITYM:
                    method = searcher.getClass().getMethod("memorySearch", String.class);
                    break;
                case DbSearcher.BTREE_ALGORITHM:
                default:
                    method = searcher.getClass().getMethod("btreeSearch", String.class);
            }
            DataBlock dataBlock = null;
            if (isNotIp(ip)) {
                log.warn("Error: Invalid ip address");
            }
            dataBlock  = (DataBlock) method.invoke(searcher, ip);
            return dataBlock.getRegion();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回IpInfo对象
     * @param ip
     * @return IpInfo
     */
    public IpInfo getIpInfoBean(String ip) {
        if (isIp(ip) || IsInnerIp(ip)) {
            return null;
        }
        String ipInfo = getIpInfo(ip);
        IpInfo info = new IpInfo();
        if (StrUtils.isNotEmpty(ipInfo)) {
            //国家|区域|省份|城市|ISP
            String[] split = ipInfo.split("|");
            info.setCountry(split[0]);
            info.setRegion(split[1]);
            info.setProvince(split[2]);
            info.setCity(split[3]);
            info.setIsp(split[4]);
        }
        return info;
    }

    /**
     * 获取客ip所在省份
     *
     * @param ip 请求
     * @return 省份城市|ISP | 或国外国家省份城市|ISP
     */
    public static String getAddress(String ip) {
        if (isNotIp(ip)) {
            return "未知";
        } else if (IsInnerIp(ip)) {
            return "内网IP";
        }
        //国家|区域|省份|城市|ISP
        String ipInfo = getIpInfo(ip);
        if (StrUtils.isNotEmpty(ipInfo)) {
            String[] cityList = ipInfo.split("\\|");
            // 国内的显示到具体的省
            if ("中国".equals(cityList[0])) {
                return cityList[2] + cityList[3] + "|" + cityList[4];
            }
            // 国外
            return cityList[0] + cityList[2] + cityList[3] + "|" + cityList[4];
        }
        return "未知";
    }

    public static String getAddress() {
        return getAddress(getClientIP());
    }

    /**
     * 私有IP：
     * A类  10.0.0.0-10.255.255.255
     * B类  172.16.0.0-172.31.255.255
     * C类  192.168.0.0-192.168.255.255
     *
     * 127这个网段是环回地址
     * localhost
     */
    static List<Pattern> ipFilterRegexList = new ArrayList<>();

    static {
        Set<String> ipFilter = new HashSet<String>();
        ipFilter.add("^10\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])"
                + "\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])" + "\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        // B类地址范围: 172.16.0.0---172.31.255.255
        ipFilter.add("^172\\.(1[6789]|2[0-9]|3[01])\\" + ".(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\"
                + ".(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        // C类地址范围: 192.168.0.0---192.168.255.255
        ipFilter.add("^192\\.168\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\"
                + ".(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        ipFilter.add("127.0.0.1");
        ipFilter.add("0.0.0.0");
        ipFilter.add("localhost");
        for (String tmp : ipFilter) {
            ipFilterRegexList.add(Pattern.compile(tmp));
        }
    }

    /**
     * ip地址模糊匹配
     * @param ip
     * @param partialIP
     * @return
     */
    public static boolean isIPInRange(String ip, String partialIP) {
        String[] ipParts = ip.split("\\.");
        String[] partialIPParts = partialIP.split("\\.");
        for (int i = 0; i < partialIPParts.length; i++) {
            if (!(partialIPParts[i].equals("*") || partialIPParts[i].equals(ipParts[i]))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断IP是否内网IP
     * @Title: ipIsInner
     * @param ip
     * @return: boolean
     */
    public static boolean IsInnerIp(String ip) {
        if (isNotIp(ip)) {
            throw new RuntimeException(ip + ": 地址不合法");
        }
        boolean isInnerIp = false;
        for (Pattern tmp : ipFilterRegexList) {
            Matcher matcher = tmp.matcher(ip);
            if (matcher.find()) {
                isInnerIp = true;
                break;
            }
        }
        return isInnerIp;
    }

    public static String getClientIP(HttpServletRequest request) {
        String[] headers = new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        String ip = "unknown";
        for (String header : headers) {
            if (StrUtils.isEmpty(ip) || ("unknown".equalsIgnoreCase(ip))) {
                ip = request.getHeader(header);
            }
        }
        if (StrUtils.isEmpty(ip) || ("unknown".equalsIgnoreCase(ip))) {
            ip = request.getRemoteAddr();
        }
        if (ip.contains(",")) {
            return ip.split(",")[0];
        } else {
            return ip;
        }
    }

    public static String getClientIP() {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return null;
        }
        return getClientIP(request);
    }

}
