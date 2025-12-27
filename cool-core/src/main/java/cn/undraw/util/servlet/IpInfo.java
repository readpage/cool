package cn.undraw.util.servlet;

import lombok.Data;

/**
 * @author readpage
 * @date 2022-11-29 21:51
 */
@Data
public class IpInfo {
    /**
     * 国家
     */
    private String country;
    /**
     * 地区
     */
    private String region;
    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 运营商
     */
    private String isp;
}
