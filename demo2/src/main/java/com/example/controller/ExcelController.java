package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.entity.User;
import com.example.service.OptionService;
import com.example.service.UserService;
import com.example.template.util.FilterParam;
import com.example.util.excel.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Excel 导入导出
 */
@Tag(name = "Excel 导出")
@RestController
@RequestMapping("/excel")
public class ExcelController {

    @Resource
    private UserService userService;

    @Resource
    private OptionService optionService;

    @Operation(summary = "动态表头导出 Excel")
    @PostMapping("/export")
    public void export(@RequestBody FilterParam param, HttpServletResponse response) throws IOException {
        List<User> users = userService.list(param);
        ExcelUtils.export(response, "导出数据", param, users);
    }

    @Operation(summary = "下载导入模板 Excel")
    @PostMapping("/template")
    public void template(HttpServletResponse response) throws IOException {
        String mdTemplate = """
            |用户名|年龄|性别|电话号码|
            |----|----|----|----|
            |张三| 25 | 男 | 13800138000|
            """;
        ExcelUtils.exportImporterTemplate(response, "用户导入模板", mdTemplate);
    }

    @Operation(summary = "Excel 导入 — 根据 columns 匹配表头，remote-select 列自动 label→value 转换")
    @PostMapping(value = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public R importExcel(
            @Parameter(description = "Excel 文件") @RequestPart("file") MultipartFile file,
            @Parameter(description = "列定义（FilterParam JSON）") @RequestParam("filterParam") String filterParamJson) {
        // 解析 Excel → 实体列表（columns 校验 + remote-select 映射加载内置 + filterParam JSON 解析）
        List<User> users;
        try {
            users = ExcelUtils.importData(file, filterParamJson,
                    (type, limit) -> optionService.listByType(type, limit), User.class);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }

        if (users.isEmpty()) {
            return R.fail("未解析到任何有效数据");
        }

        // 批量入库
        return userService.importBatch(users).toR();
    }

}
