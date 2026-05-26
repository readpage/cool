package com.example.controller;

import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.example.domain.model.Demo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static cn.undraw.util.log.enums.OperateTypeEnum.CREATE;

/**
 * @author readpage
 * @date 2023-07-05 9:20
 */
@Tag(name = "测试")
@Validated
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public R hello() {
        return R.ok("hello world!");
    }

    @Operation(summary = "参数校验异常")
    @Parameters({
            @Parameter(name = "startTime", description = "开始时间", required = true),
    })
    @GetMapping("/valid3")
    public R valid(@NotNull(message = "开始时间为必填项，不得为空") LocalDateTime startTime) {
        return R.ok(startTime);
    }

    @Operation(summary = "添加实例")
    @OperateLog(type = CREATE)
    @PostMapping("/save")
    public R save(@Valid @RequestBody Demo demo) {
        return R.ok(demo);
    }

}
