package com.undraw.controller;


import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.undraw.domain.dto.ValidDTO;
import com.undraw.domain.entity.User;
import com.undraw.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static cn.undraw.util.log.enums.OperateTypeEnum.READ;

/**
 * @author readpage
 * @date 2023-04-08 21:59
 */
@RestController
@Validated
@Api(tags = "异常管理")
@RequestMapping("/exception")
public class ExceptionController {

    @Resource
    public UserService userService;

    @ApiOperation("sql语法异常")
    @OperateLog(type = READ)
    @GetMapping("/badSql")
    public R badSql(String arg) {
        return R.ok(userService.badSql());
    }

    @ApiOperation("sql重复建异常")
    @OperateLog(type = READ)
    @GetMapping("/duplicateKey")
    public R duplicateKey() {
        User user = new User();
        user.setId(1);
        return R.ok(userService.save(user));
    }

    @ApiOperation("自定义异常")
    @OperateLog(type = READ)
    @GetMapping("/customer")
    public R customer() {
        throw new CustomerException("自定义异常");
    }

    @ApiOperation("系统异常")
    @OperateLog(type = READ)
    @GetMapping("/system")
    public R system() {
        throw new RuntimeException("系统异常");
    }

    @ApiOperation("参数校验异常")
    @GetMapping("/valid")
    public R valid(@Valid ValidDTO validDTO) {
        return R.ok(validDTO);
    }

    @ApiOperation("参数校验异常2")
    @PostMapping("/valid2")
    public R valid2(@Valid @RequestBody ValidDTO validDTO) {
        return R.ok(validDTO);
    }

    @ApiOperation("参数校验异常3")
    @GetMapping("/valid3")
    public R valid3(@NotNull(message = "开始时间为必填项，不得为空") LocalDateTime startTime) {
        return R.ok(startTime);
    }
}
