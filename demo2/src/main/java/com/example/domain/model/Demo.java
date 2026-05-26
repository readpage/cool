package com.example.domain.model;

import cn.undraw.util.phone.annotation.Phone;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author readpage
 * @date 2023-12-26 9:06
 */
@Data
@NoArgsConstructor
@Schema(title = "Demo实例")
public class Demo {

    private Integer id;

    @Schema(title = "用户名")
    @NotNull(message = "用户名为必填项，不得为空")
    private String username;

    @Schema(title = "密码")
    // 只能验证字符串，不为null、去除首尾空白字符后不能为empty
    @NotBlank(message = "密码为必填项，不得为空")
    private String password;

    @Schema(title = "年龄")
    @Min(message = "年龄不能小于10", value = 10)
    @Max(message = "年龄不能大于100", value = 100)
    private Integer age;

    @Schema(title = "身份证")
    @Size(min = 18, max = 18, message = "身份证格式不正确")
    private String idCard;

    @Schema(title = "电话号码")
    @Phone
    private String phone;

    @Schema(title = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createTime;

    @Schema(title = "修改时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updateTime;

    public Demo(Integer id) {
        Student student = new Student();
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Demo demo = (Demo) o;
        return Objects.equals(username, demo.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
