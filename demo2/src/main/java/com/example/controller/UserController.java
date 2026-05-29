package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.entity.User;
import com.example.service.UserService;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author readpage
 * @date 2023-07-05 9:20
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "动态筛选查询用户列表")
    @PostMapping("/list")
    public R<List<User>> list(@RequestBody FilterParam param) {
        return R.ok(userService.list(param));
    }

    @Operation(summary = "动态筛选分页查询用户列表")
    @PostMapping("/page")
    public R<PageResult<User>> page(@RequestBody FilterParam param) {
        return R.ok(userService.page(param));
    }

    @Operation(summary = "保存用户（id=null 新增，id≠null 修改）")
    @PostMapping
    public R<Boolean> save(@RequestBody User user) {
        return R.ok(userService.saveOrUpdate(user));
    }

    @Operation(summary = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(userService.removeById(id));
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    public R<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        return R.ok(userService.removeByIds(ids));
    }

}
