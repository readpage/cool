package com.example.controller;

import cn.undraw.util.result.R;
import cn.undraw.util.result.ResultEnum;
import com.example.domain.entity.SysConfig;
import com.example.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "配置管理", description = "通用配置 CRUD 接口")
@RestController
@RequestMapping("/config")
public class ConfigController {

    /** 固定用户ID（后续接入认证后改为动态获取） */
    private static final Long FIXED_USER_ID = 1L;

    @Resource
    private SysConfigService sysConfigService;

    @Operation(summary = "保存配置 - 按业务键 (configGroup, configKey, userId, deleted) UPSERT")
    @PostMapping("/user/save")
    public R<SysConfig> save(@RequestBody SysConfig config) {
        boolean isAdmin = true;
        if (!isAdmin && config.getUserId() != null && config.getUserId() == 0) {
            return R.fail(ResultEnum.NO_PERMISSION);
        }
        if (!isAdmin) {
            config.setUserId(FIXED_USER_ID);
        }
        sysConfigService.upsert(config);
        return R.ok(config);
    }


    @Operation(summary = "查询系统默认配置（userId=0），按 configGroup / configKey 筛选")
    @GetMapping("/user/system")
    public R<SysConfig> system(
            @Parameter(description = "配置分组") @RequestParam(required = false) String configGroup,
            @Parameter(description = "配置标识") @RequestParam(required = false) String configKey) {
        return R.ok(sysConfigService.getSystemConfig(configGroup, configKey));
    }

    @Operation(summary = "查询我的配置，按 configGroup / configKey 筛选")
    @GetMapping("/user/my")
    public R<SysConfig> my(
            @Parameter(description = "配置分组") @RequestParam(required = false) String configGroup,
            @Parameter(description = "配置标识") @RequestParam(required = false) String configKey) {
        return R.ok(sysConfigService.getUserConfig(FIXED_USER_ID, configGroup, configKey));
    }
}
