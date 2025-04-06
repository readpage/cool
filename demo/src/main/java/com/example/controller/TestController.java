package com.example.controller;

import cn.undraw.util.result.R;
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
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public R hello() {
        return R.ok("hello world!");
    }


}
