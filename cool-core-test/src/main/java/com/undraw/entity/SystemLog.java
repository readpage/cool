package com.undraw.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

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
@ApiModel(value = "SystemLog对象", description = "")
public class SystemLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("操作模块")
    private String module;

    @ApiModelProperty("操作名")
    private String name;

    @ApiModelProperty("操作分类")
    private Integer type;

    @ApiModelProperty("请求方法方式")
    private String requestMethod;

    @ApiModelProperty("请求方法")
    private String optMethod;

    @ApiModelProperty("请求地址")
    private String requestUrl;

    @ApiModelProperty("请求参数")
    private String requestParam;

    @ApiModelProperty("用户 IP")
    private String userIp;

    @ApiModelProperty("地理位置")
    private String address;

    @ApiModelProperty("浏览器 UserAgent")
    private String userAgent;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("执行时长，单位：毫秒")
    private String duration;

    @ApiModelProperty("结果码")
    private String resultCode;

    @ApiModelProperty("结果提示")
    private String resultMsg;

    @ApiModelProperty("结果数据")
    private String resultData;


}
