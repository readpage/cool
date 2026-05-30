package com.example.controller;

import cn.undraw.util.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.domain.entity.SysConfig;
import com.example.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "配置管理", description = "通用配置 CRUD 接口")
@RestController
@RequestMapping("/config")
public class ConfigController {

    @Resource
    private SysConfigService sysConfigService;

    // ==================== 基础 CRUD ====================

    @Operation(summary = "保存配置（id=null 新增，id≠null 修改，userId=0 为系统默认），返回实体含自增ID")
    @PostMapping("/save")
    public R<SysConfig> save(@RequestBody SysConfig config) {
        sysConfigService.saveOrUpdate(config);
        return R.ok(config);
    }

    @Operation(summary = "根据ID删除配置")
    @DeleteMapping("/remove/{id}")
    public R<Boolean> remove(@Parameter(description = "配置ID") @PathVariable Long id) {
        return R.ok(sysConfigService.removeById(id));
    }

    @Operation(summary = "根据ID查询配置")
    @GetMapping("/get/{id}")
    public R<SysConfig> getById(@Parameter(description = "配置ID") @PathVariable Long id) {
        return R.ok(sysConfigService.getById(id));
    }

    @Operation(summary = "查询配置列表（按 configGroup / configKey 筛选，同时返回系统默认 userId=0 + 当前用户配置）")
    @GetMapping("/list")
    public R<List<SysConfig>> list(
            @Parameter(description = "配置分组") @RequestParam(required = false) String configGroup,
            @Parameter(description = "配置标识") @RequestParam(required = false) String configKey) {
        // TODO: 后续从 token 获取
        Long currentUserId = 1L;
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (configGroup != null) wrapper.eq(SysConfig::getConfigGroup, configGroup);
        if (configKey != null) wrapper.eq(SysConfig::getConfigKey, configKey);
        wrapper.in(SysConfig::getUserId, 0L, currentUserId);
        return R.ok(sysConfigService.list(wrapper));
    }
}
