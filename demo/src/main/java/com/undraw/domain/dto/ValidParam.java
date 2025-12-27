package com.undraw.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author readpage
 * @date 2023-04-21 14:20
 */
@Getter
@Setter
@Schema(title = "校验参数实例对象")
public class ValidParam {

    //  适用于基本数据类型（如 Integer, Long, Double 等）以及引用类型。它表示被注解的元素不能为NULL， 无法查检长度为0的字符串
    @NotNull(message = "id为必填项，不得为空")
    private Integer id;

    // @NotBlank注解，仅用于校验字符串字段，对象不为null，且经过trim()去除首尾空格方法处理后的长度大于0。
    @NotBlank(message = "msg为必填项，不得为空")
    @Size(min = 1, max = 10, message="长度必须在1至10之间")
    private String msg;

    // @NotEmpty 可以用于 String 类型、集合类型（如 List, Set）、Map 以及 Array校验。
    // 当用于 String 类型时，表示该字段不能为空字符串（""），即长度必须大于 0。
    // 当用于集合类型时，表示集合不能为 null 且集合的大小（size）必须大于 0
    private int number;

    @Size(min = 2, max = 2, message="日期格式错误")
    private LocalDate[] date;

    @NotNull
    @Size(min = 1, message = "列表不能为空")
    private List<String> list;

    private LocalDateTime localDateTime;

}
