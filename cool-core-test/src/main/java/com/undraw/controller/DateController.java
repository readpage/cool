package com.undraw.controller;

import cn.undraw.util.result.R;
import com.undraw.domain.dto.DateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    public R test2(LocalDate date) {
        return R.ok(date);
    }
    @Operation(summary = "测试1")
    @GetMapping("/test")
    public R test(DateDTO dateDTO) {
        return R.ok(dateDTO);
    }


    @Operation(summary = "测试2")
    @GetMapping("/test2")
    public R test2() {
        LocalDate now = LocalDate.now();
        LocalDateTime now2 = LocalDateTime.now();
//        DateDTO dateDTO = new DateDTO(now2, now);
        return R.ok();
    }

   @Operation(summary = "测试3")
    @PostMapping("/test3")
    public R test2(@RequestBody DateDTO testDTO) {
        return R.ok(testDTO);
    }
}
