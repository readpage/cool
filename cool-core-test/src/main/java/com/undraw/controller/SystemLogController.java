package com.undraw.controller;


import cn.undraw.util.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:08
 */
@RestController
@RequestMapping("/system-log")
public class SystemLogController {

    @GetMapping("/test")
    public R test(HttpServletRequest request) {
        return R.ok();
    }
}

