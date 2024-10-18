package com.undraw.domain.entity;

import cn.undraw.util.phone.annotation.Phone;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:00
 */
@Getter
@Setter
@Schema(title = "User对象", description = "用户对象")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(title = "用户名")
    @NotNull(message = "用户名为必填项，不得为空")
    private String username;

    @Schema(title = "密码")
    @NotBlank(message = "密码为必填项，不得为空")
    private String password;

    @Schema(title = "年龄")
    private Integer age;

    @Phone
    @Schema(title = "电话号码")
    private String phone;

    @Schema(title = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(title = "修改时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
