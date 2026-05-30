package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.entity.User;
import com.example.service.UserService;
import com.example.template.util.FilterParam;
import com.example.template.util.PageResult;
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
 * @author readpage
 * @date 2023-07-05 9:20
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "查询用户列表")
    @PostMapping("/list")
    public R<List<User>> list(@RequestBody FilterParam param) {
        return R.ok(userService.list(param));
    }

    @Operation(summary = "分页查询用户列表")
    @PostMapping("/page")
    public R<PageResult<User>> page(@RequestBody FilterParam param) {
        return R.ok(userService.page(param));
    }

    @Operation(summary = "保存用户（id=null 新增，id≠null 修改）")
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody User user) {
        return R.ok(userService.saveOrUpdate(user));
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/remove")
    public R<Boolean> remove(@RequestBody List<Long> ids) {
        return R.ok(userService.removeByIds(ids));
    }

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
        return userService.importExcel(file, filterParamJson).toR();
    }

}
