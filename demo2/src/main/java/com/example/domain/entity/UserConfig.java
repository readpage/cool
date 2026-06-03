package com.example.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_config")
@Schema(title = "UserConfig对象", description = "用户偏好配置对象")
public class UserConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "配置分组：table / bi / system")
    private String configGroup;

    @Schema(description = "配置标识：user / report_001 / theme")
    private String configKey;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "配置 JSON 内容")
    private String configValue;

    @Schema(description = "0=正常, 1=回收站（软删除）")
    private Integer deleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
