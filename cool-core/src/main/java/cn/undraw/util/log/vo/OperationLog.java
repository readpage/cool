package cn.undraw.util.log.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author readpage
 * @date 2022-11-29 18:47
 */
@Data
public class OperationLog {

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作名
     */
    private String name;

    /**
     * 操作分类
     */
    private Integer type;

    /**
     * 请求方法方式
     */
    private String requestMethod;

    /**
     * 请求方法
     */
    private String optMethod;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 地理位置
     */
    private String address;

    /**
     * 设备 device
     */
    private String device;

    /**
     * 浏览器 UserAgent
     */
    private String userAgent;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 执行时长，单位：毫秒
     */
    private long duration;

    /**
     * 结果码
     */
    private Integer resultCode;

    /**
     * 结果提示
     */
    private String resultMsg;

    /**
     * 结果数据
     */
    private String resultData;
}
