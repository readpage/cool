package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.dto.ConfigQuery;
import com.example.domain.dto.ConfigResult;
import com.example.domain.entity.SysConfig;
import com.example.domain.entity.UserConfig;
import com.example.service.SysConfigService;
import com.example.service.UserConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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

    // ==================== 版本检查（后续用于新版本通知用户重置） ====================

    @Operation(summary = "检查系统配置版本 — 返回 sys_config 当前版本，前端比较后通知用户重置")
    @GetMapping("/version")
    public R<ConfigResult> checkVersion(
            @Parameter(description = "配置分组") @RequestParam String configGroup,
            @Parameter(description = "配置标识") @RequestParam String configKey) {
        SysConfig sc = sysConfigService.getSystemConfig(configGroup, configKey);
        if (sc == null || sc.getConfigValue() == null) {
            return R.ok(new ConfigResult());
        }
        return R.ok(ConfigResult.from(sc));
    }

    // ==================== 恢复系统默认 ====================

    @Operation(summary = "恢复系统默认 — 用 sys_config 覆盖 user_config，返回系统配置。\n系统版本更新时，前端可调用 /config/version 检查后引导用户调用此接口重置。")
    @PostMapping("/user/reset")
    public R<ConfigResult> reset(@RequestBody ConfigQuery query) {
        String configGroup = query.getConfigGroup();
        String configKey = query.getConfigKey();

        // 1) 先软删除当前用户的偏好配置（无论系统配置是否存在，都必须清除）
        userConfigService.lambdaUpdate()
                .eq(UserConfig::getConfigGroup, configGroup)
                .eq(UserConfig::getConfigKey, configKey)
                .eq(UserConfig::getUserId, FIXED_USER_ID)
                .set(UserConfig::getDeleted, 1)
                .update();

        // 2) 读取系统配置
        SysConfig sc = sysConfigService.getSystemConfig(configGroup, configKey);
        if (sc == null || sc.getConfigValue() == null) {
            // 系统配置不存在 → 返回空结果，前端自行用报表 API 加载原始配置
            log.info("reset: sys_config 不存在 (group={} key={})，已清除 user_config，返回空结果",
                    configGroup, configKey);
            return R.ok(new ConfigResult());
        }

        // 3) 用系统配置覆盖 user_config（软删除后重新写入）
        UserConfig uc = new UserConfig();
        uc.setConfigGroup(configGroup);
        uc.setConfigKey(configKey);
        uc.setUserId(FIXED_USER_ID);
        uc.setConfigValue(sc.getConfigValue());
        userConfigService.upsert(uc);

        log.info("reset: 已用 sys_config(v{}) 覆盖 user_config (group={} key={})",
                sc.getVersion(), configGroup, configKey);

        // 4) 返回系统配置（含版本号，前端可缓存用于后续版本比较）
        return R.ok(ConfigResult.from(sc));
    }
}
