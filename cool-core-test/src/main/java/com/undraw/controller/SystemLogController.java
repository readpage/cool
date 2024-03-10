package com.undraw.controller;


import cn.undraw.util.result.R;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
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
        //解析agent字符串
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        //获取浏览器对象
        Browser browser = userAgent.getBrowser();
        System.out.println(browser);
        //获取操作系统对象
        OperatingSystem os = userAgent.getOperatingSystem();
        System.out.println(request.getHeader("User-Agent"));
        return R.ok();
    }
}

