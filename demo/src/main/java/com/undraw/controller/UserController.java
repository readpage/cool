package com.undraw.controller;


import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.log.enums.OperateTypeEnum;
import cn.undraw.util.result.R;
import com.undraw.domain.dto.UserParam;
import com.undraw.domain.entity.User;
import com.undraw.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static cn.undraw.util.log.enums.OperateTypeEnum.CREATE;
import static cn.undraw.util.log.enums.OperateTypeEnum.UPDATE;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:00
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理")
public class UserController {

    @Resource
    private UserService userService;


    @Operation(summary = "分页查询用户信息")
    @GetMapping("/page")
    public R page(UserParam obj) {
        return R.ok(userService.page(obj));
    }

    @Operation(summary = "添加用户")
    @OperateLog(type = CREATE)
    @PostMapping("/save")
    public R save(@Valid @RequestBody User user) {
        return R.ok(userService.save(user));
    }

    @Operation(summary = "修改用户")
    @OperateLog(type = UPDATE)
    @PutMapping("/update")
    public R update(@Valid @RequestBody User user) {
        return R.ok(userService.updateById(user));
    }

    @Operation(summary = "异常sql")
    @GetMapping("/badSql")
    public R badSql() {
        return R.ok(userService.badSql());
    }

    // consumes：指定控制器支持的请求媒体类型。
    @Operation(summary = "导入用户信息")
    @OperateLog(type = OperateTypeEnum.IMPORT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R upload(@RequestBody MultipartFile file) {
        return R.ok(userService.upload(file));
    }

    @Operation(summary = "导出用户信息")
    @OperateLog(type = OperateTypeEnum.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, UserParam obj) {
        userService.export(response, obj);
    }

}

