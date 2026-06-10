package com.example.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 配置查询/恢复请求参数
 */
@Data
@Schema(title = "配置查询参数")
public class ConfigQuery {

    @Schema(title = "配置分组", example = "table")
    private String configGroup;

    @Schema(title = "配置标识", example = "crud-demo")
    private String configKey;
}
