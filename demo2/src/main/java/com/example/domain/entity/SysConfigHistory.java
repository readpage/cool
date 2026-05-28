package com.example.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(title = "SysConfigHistory对象", description = "系统默认配置变更历史对象")
public class SysConfigHistory implements Serializable {

    private Long id;

    @Schema(description = "配置分组")
    private String configGroup;

    @Schema(description = "配置标识")
    private String configKey;

    @Schema(description = "快照版本号（单调递增）")
    private Integer version;

    @Schema(description = "该版本 configValue 全量快照 JSON")
    private String snapshot;

    @Schema(description = "SAVE(保存) / ROLLBACK(回退)")
    private String changeType;

    @Schema(description = "回退来源版本号")
    private Integer rollbackFrom;

    @Schema(description = "操作人 ID")
    private Long changedBy;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
