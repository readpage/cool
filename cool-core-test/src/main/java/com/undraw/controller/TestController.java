package com.undraw.controller;

import cn.undraw.util.result.R;
import cn.undraw.util.servlet.ServletUtils;
import com.undraw.domain.model.Model;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

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
    @Parameters({
            @Parameter(name = "daily", description = "日期"),
    })
    public R hello(@RequestParam(required = false) LocalDate daily) {
        HttpServletRequest request = ServletUtils.getRequest();
        String header = request.getHeader("User-Agent");
        System.out.println(header);
        String userAgent = ServletUtils.getUserAgent();
        return R.ok("hello world! " + daily);
    }

    @PostMapping("/model")
    public R model(@RequestBody Model model) {
        return R.ok(model);
    }

}
