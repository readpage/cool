package com.example.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "DemoParam对象")
public class DemoParam {

    @Schema(title = "姓名")
    private String name;

    @Schema(title = "手机号")
    private String phone;
}