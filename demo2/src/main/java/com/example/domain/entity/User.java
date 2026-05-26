package com.example.domain.entity;

import cn.undraw.util.phone.annotation.Phone;
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
@Schema(title = "User对象", description = "用户对象")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

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

    private Double salary = 22.3578;

    private List<Long> ridList;

    @Schema(title = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createTime;

    @Schema(title = "修改时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updateTime;

    public User() {
    }

    public User(String password, Integer age) {
        this.password = password;
        this.age = age;
    }
}
