package com.undraw.controller;

import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.Result;
import cn.undraw.util.result.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.undraw.util.log.enums.OperateTypeEnum.READ;


/**
 * @author readpage
 * @date 2022-12-08 8:25
 */

@RestController
@RequestMapping("/hello")
public class HelloController {
    @OperateLog(type = READ)
    @GetMapping("/hello")
    public Result<?> hello() {
        return ResultUtils.ok("hello world!");
    }
}
