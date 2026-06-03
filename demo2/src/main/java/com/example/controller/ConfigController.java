package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.entity.SysConfig;
import com.example.domain.entity.UserConfig;
import com.example.service.SysConfigService;
import com.example.service.UserConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "配置管理", description = "系统默认配置 + 用户偏好配置 CRUD 接口")
@RestController
@RequestMapping("/config")
public class ConfigController {

    /** 固定用户ID（后续接入认证后改为动态获取） */
    private static final Long FIXED_USER_ID = 1L;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private UserConfigService userConfigService;

    // ==================== 系统默认配置 ====================

    @Operation(summary = "查询系统默认配置，按 configGroup / configKey 筛选")
    @GetMapping("/user/system")
    public R<SysConfig> system(
            @Parameter(description = "配置分组") @RequestParam(required = false) String configGroup,
            @Parameter(description = "配置标识") @RequestParam(required = false) String configKey) {
        return R.ok(sysConfigService.getSystemConfig(configGroup, configKey));
    }

    @Operation(summary = "列出指定分组的所有系统默认配置")
    @GetMapping("/system/list")
    public R<List<SysConfig>> systemList(
            @Parameter(description = "配置分组") @RequestParam String configGroup) {
        return R.ok(sysConfigService.listByGroup(configGroup));
    }

    @Operation(summary = "保存系统默认配置 - 按业务键 (configGroup, configKey) UPSERT")
    @PostMapping("/system/save")
    public R<SysConfig> systemSave(@RequestBody SysConfig config) {
        sysConfigService.upsert(config);
        return R.ok(config);
    }

    // ==================== 用户偏好配置 ====================

    @Operation(summary = "保存用户偏好配置 - 按业务键 (configGroup, configKey, userId) UPSERT")
    @PostMapping("/user/save")
    public R<UserConfig> save(@RequestBody UserConfig config) {
        if (config.getUserId() == null || config.getUserId() == 0) {
            config.setUserId(FIXED_USER_ID);
        }
        userConfigService.upsert(config);
        return R.ok(config);
    }

    @Operation(summary = "查询当前用户偏好配置，按 configGroup / configKey 筛选")
    @GetMapping("/user/my")
    public R<UserConfig> my(
            @Parameter(description = "配置分组") @RequestParam(required = false) String configGroup,
            @Parameter(description = "配置标识") @RequestParam(required = false) String configKey) {
        return R.ok(userConfigService.getUserConfig(FIXED_USER_ID, configGroup, configKey));
    }

    @Operation(summary = "列出当前用户指定分组的所有偏好配置")
    @GetMapping("/user/list")
    public R<List<UserConfig>> userList(
            @Parameter(description = "配置分组") @RequestParam String configGroup) {
        return R.ok(userConfigService.listByUserAndGroup(FIXED_USER_ID, configGroup));
    }
}
