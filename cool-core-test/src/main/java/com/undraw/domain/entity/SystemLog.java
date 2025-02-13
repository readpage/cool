package com.undraw.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:08
 */
@Getter
@Setter
@TableName("system_log")
@Schema(title = "SystemLog对象")
public class SystemLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(title = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(title = "操作模块")
    private String module;

    @Schema(title = "操作名")
    private String name;

    @Schema(title = "操作分类")
    private Integer type;

    @Schema(title = "请求方法方式")
    private String requestMethod;

    @Schema(title = "请求方法")
    private String optMethod;

    @Schema(title = "请求地址")
    private String requestUrl;

    @Schema(title = "请求参数")
    private String requestParam;

    @Schema(title = "用户 IP")
    private String userIp;

    @Schema(title = "地理位置")
    private String address;

    @Schema(title = "设备")
    private String device;

    @Schema(title = "浏览器 UserAgent")
    private String userAgent;

    @Schema(title = "开始时间")
    private LocalDateTime startTime;

    @Schema(title = "执行时长，单位：毫秒")
    private String duration;

    @Schema(title = "结果码")
    private String resultCode;

    @Schema(title = "结果提示")
    private String resultMsg;

    @Schema(title = "结果数据")
    private String resultData;


}
