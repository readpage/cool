package com.undraw.controller;

//import cn.undraw.handler.xss.XssHttpServletRequest;

import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

import static cn.undraw.util.log.enums.OperateTypeEnum.CREATE;
import static cn.undraw.util.log.enums.OperateTypeEnum.READ;

/**
 * @author readpage
 * @date 2023-03-03 13:20
 */
@RestController
@RequestMapping("/filter")
@Tag(name = "过滤处理")
public class FilterController {

    @Operation(summary = "参数拦截过滤")
    @OperateLog(type = READ)
    @GetMapping("/args")
    public R args(String name) {
        return R.ok(name);
    }

    @PostMapping("/map")
    @OperateLog(type = CREATE)
    public R test(@RequestBody Map map) throws IOException {
        return R.ok(map);
    }
}
