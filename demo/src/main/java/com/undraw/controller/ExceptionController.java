package com.undraw.controller;


import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.undraw.domain.dto.ValidParam;
import com.undraw.domain.entity.User;
import com.undraw.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static cn.undraw.util.log.enums.OperateTypeEnum.READ;

/**
 * @author readpage
 * @date 2023-04-08 21:59
 */
@RestController
@Validated
@Tag(name = "异常管理")
@RequestMapping("/exception")
public class ExceptionController {

    @Resource
    public UserService userService;

    @Operation(summary = "sql语法异常")
    @OperateLog(type = READ)
    @GetMapping("/badSql")
    public R badSql() {
        return R.ok(userService.badSql());
    }

    @Operation(summary = "sql重复建异常")
    @OperateLog(type = READ)
    @GetMapping("/duplicateKey")
    public R duplicateKey() {
        User user = new User();
        user.setId(1L);
        return R.ok(userService.save(user));
    }

    @Operation(summary = "自定义异常")
    @OperateLog(type = READ)
    @GetMapping("/customer")
    public R customer() {
        throw new CustomerException("自定义异常");
    }

    @Operation(summary = "系统异常")
    @OperateLog(type = READ)
    @GetMapping("/system")
    public R system() {
        throw new RuntimeException("系统异常");
    }

    @Operation(summary = "参数校验异常")
    @GetMapping("/valid")
    public R valid(@Valid ValidParam obj) {
        return R.ok(obj);
    }

    @Operation(summary = "参数校验异常2")
    @PostMapping("/valid2")
    public R valid2(@Valid @RequestBody ValidParam obj) {
        return R.ok(obj);
    }

    @Operation(summary = "参数校验异常3")
    @GetMapping("/valid3")
    public R valid3(@NotNull(message = "开始时间为必填项，不得为空") @RequestParam("startTime") LocalDateTime startTime) {
        return R.ok(startTime);
    }
}
