package com.undraw.controller;


import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.undraw.entity.User;
import com.undraw.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.undraw.util.log.enums.OperateTypeEnum.CREATE;

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
@Api(tags = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("添加用户")
    @OperateLog(type = CREATE)
    @PostMapping("/save")
    public R save(@Valid @RequestBody User user) {
        return R.ok(userService.save(user));
    }

}

