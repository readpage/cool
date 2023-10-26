package com.undraw.controller;

import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.undraw.util.log.enums.OperateTypeEnum.*;

/**
 * @author readpage
 * @date 2023-03-03 13:20
 */
@RestController
@RequestMapping("/filter")
@Api(tags = "过滤处理")
public class FilterController {

    @ApiOperation("参数拦截过滤")
    @OperateLog(type = READ)
    @GetMapping("/args")
    public R args(String name) {
        return R.ok(name);
    }

    @PostMapping("/map")
    @OperateLog(type = CREATE)
    public R test(@RequestBody Map map) {
        return R.ok(map);
    }
}
