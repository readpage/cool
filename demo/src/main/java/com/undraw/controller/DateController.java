package com.undraw.controller;

import cn.undraw.util.result.R;
import com.undraw.domain.dto.DateParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * cn.undraw.config.TimeFormatConfig 日期格式化
 * @author readpage
 * @date 2023-01-16 14:16
 */
@Tag(name = "日期测试")
@RequestMapping("/date")
@RestController
public class DateController {

    @Operation(summary = "测试0")
    @GetMapping("/test0")
    public R test0(LocalDate date) {
        return R.ok(date);
    }

    @Operation(summary = "测试1")
    @GetMapping("/test")
    public R test(DateParam obj) {
        return R.ok(obj);
    }

   @Operation(summary = "测试2")
    @PostMapping("/test2")
    public R test2(@RequestBody DateParam obj) {
        return R.ok(obj);
    }
}
