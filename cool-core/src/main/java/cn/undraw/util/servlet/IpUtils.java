package cn.undraw.util.servlet;

import cn.undraw.util.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletRequest;
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

    /**
     *
     * @param ip
     * @return
     */
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
     * ??????????????????
     * @param ip ??????ip
     * @param algorithm
     * @return ??????|??????|??????|??????|ISP
     */
    public static String getIpInfo(String ip, int... algorithm){
        if (StrUtils.isEmpty(algorithm)) {
            algorithm = new int[]{3};
        }
        try {
            //        ????????????
//        1.memory???????????????????????????????????????????????????????????????0.1x????????????C?????????????????????????????????0.00x???????????????
//        2.binary????????????????????????????????????ip2region.db????????????????????????????????????????????????0.x???????????????
//        3.b-tree???????????????btree???????????????ip2region.db????????????????????????????????????????????????0.x??????????????????binary???????????????
//        ps??????????????????b-tree??????binary??????????????????memory????????????????????????

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
     * ??????IpInfo??????
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
            //??????|??????|??????|??????|ISP
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
     * ?????????ip????????????
     *
     * @param ip ??????
     * @return ????????????|ISP | ???????????????????????????|ISP
     */
    public static String getAddress(String ip) {
        if (isNotIp(ip)) {
            return "??????";
        } else if (IsInnerIp(ip)) {
            return "??????IP";
        }
        //??????|??????|??????|??????|ISP
        String ipInfo = getIpInfo(ip);
        if (StrUtils.isNotEmpty(ipInfo)) {
            String[] cityList = ipInfo.split("\\|");
            // ??????????????????????????????
            if ("??????".equals(cityList[0])) {
                return cityList[2] + cityList[3] + "|" + cityList[4];
            }
            // ??????
            return cityList[0] + cityList[2] + cityList[3] + "|" + cityList[4];
        }
        return "??????";
    }

    public static String getAddress() {
        return getAddress(getClientIP());
    }

    /**
     * ??????IP???
     * A???  10.0.0.0-10.255.255.255
     * B???  172.16.0.0-172.31.255.255
     * C???  192.168.0.0-192.168.255.255
     *
     * 127???????????????????????????
     * localhost
     */
    static List<Pattern> ipFilterRegexList = new ArrayList<>();

    static {
        Set<String> ipFilter = new HashSet<String>();
        ipFilter.add("^10\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])"
                + "\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])" + "\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        // B???????????????: 172.16.0.0---172.31.255.255
        ipFilter.add("^172\\.(1[6789]|2[0-9]|3[01])\\" + ".(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\"
                + ".(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        // C???????????????: 192.168.0.0---192.168.255.255
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
     * ??????IP????????????IP
     * @Title: ipIsInner
     * @param ip
     * @return: boolean
     */
    public static boolean IsInnerIp(String ip) {
        if (isNotIp(ip)) {
            throw new RuntimeException(ip + ": ???????????????");
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
