package com.undraw.dto;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author readpage
 * @date 2023-04-21 14:20
 */
@Getter
@Setter
@ApiModel(value = "ValidDTO对象", description = "校验参数实例对象")
public class ValidDTO {
    @NotNull(message = "id为必填项，不得为空")
    private Integer id;

    @NotNull(message = "msg为必填项，不得为空")
    private String msg;

    private int number;

    @NotNull(message = "date为必填项，不得为空")
    private LocalDate date;

    private LocalDateTime localDateTime;
}
