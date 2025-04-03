package com.undraw.controller;


import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.undraw.domain.entity.Role;
import com.undraw.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.undraw.util.log.enums.OperateTypeEnum.CREATE;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author readpage
 * @since 2025-02-13 21:15
 */
@RestController
@Tag(name = "角色管理")
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @Operation(summary = "添加角色")
    @OperateLog(type = CREATE)
    @PostMapping("/save")
    public R save(@Valid @RequestBody Role role) {
        return R.ok(roleService.save(role));
    }

    @Operation(summary = "批量添加角色")
    @OperateLog(type = CREATE)
    @PostMapping("/saveBatch")
    public R saveBatch(@Valid @RequestBody List<Role> list) {
        return R.ok(roleService.saveBatch1(list));
    }
}

