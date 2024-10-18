package com.undraw.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author readpage
 * @date 2023-04-21 14:20
 */
@Getter
@Setter
@Schema(title = "校验参数实例对象")
public class ValidDTO {
    @NotNull(message = "id为必填项，不得为空")
    private Integer id;

    // @NotBlank注解，它结合了@NotNull和@Size(min = 1)的功能，专门用于检查字符串字段是否为空或空字符串
    // 只能验证字符串，不为null、去除首尾空白字符后不能为empty
    @NotBlank(message = "msg为必填项，不得为空")
    @Size(min = 1, max = 10, message="长度必须在1至10之间")
    private String msg;

    private int number;

    @Size(min = 2, max = 2, message="日期格式错误")
    private LocalDate[] date;

    private LocalDateTime localDateTime;

}
