package com.undraw.domain.entity;

import cn.undraw.util.phone.annotation.Phone;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 *
 * @since 2022-09-03 13:03
 */
@Data
@NoArgsConstructor
@Schema(title = "用户")
@ToString
public class User2 implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(title = "用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(title = "用户名")
    @NotNull(message = "用户名为必填项，不得为空")
    private String username;

    @Schema(title = "昵称")
    private String nickname;

    @Schema(title = "密码")
    private String password;

    @Schema(title = "状态")
    @TableField(fill = FieldFill.INSERT)
    private Boolean status;

    @Schema(title = "头像")
    private String avatar;

    @Schema(title = "邮箱")
    private String email;

    @Schema(title = "身份证号")
    private String idCard;

    @Schema(title = "手机号码")
    @Phone(required = false)
    private String phone;

    @Schema(title = "员工id")
    private Long empId;

    @Schema(title = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(title = "修改时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(title = "权限列表")
    @NotNull
    @Size(min = 1)
    @TableField(exist = false)
    private List<String> auths;

    @TableLogic
    @Schema(title = "是否删除")
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    public User2(String password) {
        this.password = password;
    }


    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
