package com.undraw.controller;

import cn.undraw.util.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author readpage
 * @date 2023-07-05 9:20
 */
@Validated
@RestController
@Tag(name = "测试")
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "hello")
    @GetMapping("/hello")
    public R hello() {
        return R.ok("hello world!");
    }


}
