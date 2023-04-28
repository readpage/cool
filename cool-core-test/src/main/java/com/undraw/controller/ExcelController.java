package com.undraw.controller;

import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.undraw.util.excel.ExcelUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;

import static cn.undraw.util.log.enums.OperateTypeEnum.EXPORT;
import static cn.undraw.util.log.enums.OperateTypeEnum.READ;


/**
 * @author readpage
 * @date 2022-12-08 8:25
 */

@RestController
@Api(tags = "excel管理")
@RequestMapping("/excel")
@Slf4j
public class ExcelController {
    @ApiOperation("hello")
    @GetMapping("/hello")
    @OperateLog(type = READ)
    public R<?> hello(String name) {
        return R.ok((Object)( "hello " + name));
    }

    @GetMapping("/export")
    @ApiOperation("导出excel")
    @OperateLog(type = EXPORT)
    public void export(HttpServletResponse response, String info) {
        log.info(info);
        ExcelUtils.export(response, "导出", "sheet", new ArrayList<>());
    }
}
