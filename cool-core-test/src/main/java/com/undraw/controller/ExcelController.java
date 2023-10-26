package com.undraw.controller;

import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.pig4cloud.plugin.excel.annotation.RequestExcel;
import com.pig4cloud.plugin.excel.vo.ErrorMessage;
import com.undraw.domain.dto.ImportDemo;
import com.undraw.domain.model.Employee;
import com.undraw.util.excel.ExcelUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static cn.undraw.util.log.enums.OperateTypeEnum.EXPORT;


/**
 * @author readpage
 * @date 2022-12-08 8:25
 */

@RestController
@Api(tags = "excel管理")
@RequestMapping("/excel")
@Slf4j
public class ExcelController {
    @ApiOperation("导入数据")
    @PostMapping("/import")
    public R upload(@RequestExcel List<ImportDemo> importDemoList, String arg, BindingResult bindingResult) {
        System.out.println(arg);
        importDemoList.forEach(System.out::println);
        // JSR 303 校验通用校验获取失败的数据
        List<ErrorMessage> errorMessageList = (List<ErrorMessage>) bindingResult.getTarget();
        return R.ok(errorMessageList);
    }

    @GetMapping("/export")
    @ApiOperation("导出excel")
    @OperateLog(type = EXPORT)
    public void export(HttpServletResponse response) {
        ExcelUtils.export(response, "导出", "sheet", Employee.employeeList, Employee.class);
    }
}
