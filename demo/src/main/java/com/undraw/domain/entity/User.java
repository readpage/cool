package com.undraw.domain.entity;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.undraw.util.decimal.annotation.DecimalFormat;
import cn.undraw.util.phone.annotation.Phone;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:00
 */
@Data
@TableName(value = "user")
@Schema(title = "User对象", description = "用户对象")
@DecimalFormat
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelIgnore
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(title = "用户名")
    @NotNull(message = "用户名为必填项，不得为空")
    @ExcelProperty(value = "用户名")
    private String username;

    @Schema(title = "密码")
    @NotBlank(message = "密码为必填项，不得为空")
    @ExcelProperty(value = "密码")
    private String password;

    @Schema(title = "年龄")
    @ExcelProperty(value = "年龄")
    private Integer age;

    @Phone
    @Schema(title = "电话号码")
    @ExcelProperty(value = "电话号码")
    private String phone;

    @TableField(exist = false)
    private Double salary = 22.3578;

    @TableField(exist = false)
    private List<Long> ridList;

    @Schema(title = "创建时间")
    @ExcelProperty(value = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(title = "修改时间")
    @ExcelProperty(value = "修改时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
