package com.undraw.controller;

import cn.undraw.util.result.R;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author readpage
 * @date 2023-03-03 13:20
 */
@RestController
@RequestMapping("/filter")
@Api(tags = "过滤处理")
public class FilterController {
    @PostMapping("/test")
    public R test(@RequestBody Map map) {
        return R.ok(map);
    }
}
